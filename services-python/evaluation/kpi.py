
import psycopg2.extras


def compute_kpis(conn):
    with conn.cursor() as cur:
        # TTFA: time from submission to first proposal, per request
        cur.execute(
            """
            SELECT AVG(EXTRACT(EPOCH FROM (first_proposed - submitted_at))),
                   PERCENTILE_CONT(0.5) WITHIN GROUP (
                       ORDER BY EXTRACT(EPOCH FROM (first_proposed - submitted_at))
                   )
            FROM (
                SELECT rl.request_id, rl.submitted_at, MIN(p.proposed_at) AS first_proposed
                FROM request_lifecycle rl
                JOIN proposals p ON p.request_id = rl.request_id
                WHERE rl.submitted_at IS NOT NULL
                GROUP BY rl.request_id, rl.submitted_at
            ) t
            """
        )
        avg_ttfa, median_ttfa = cur.fetchone()

        cur.execute(
            """
            SELECT outcome, COUNT(*) FROM proposals
            WHERE outcome != 'pending'
            GROUP BY outcome
            """
        )
        outcome_counts = dict(cur.fetchall())

        cur.execute(
            "SELECT AVG(match_score) FROM proposals WHERE outcome = 'accepted'"
        )
        (avg_pertinence,) = cur.fetchone()

        cur.execute(
            """
            SELECT AVG(proposal_count) FROM (
                SELECT request_id, COUNT(*) AS proposal_count
                FROM proposals GROUP BY request_id
            ) t
            """
        )
        (avg_proposals_per_request,) = cur.fetchone()

    total_resolved = sum(outcome_counts.values()) or 1
    return {
        "ttfa_seconds": {"avg": avg_ttfa, "median": median_ttfa},
        "acceptance_rate": outcome_counts.get("accepted", 0) / total_resolved,
        "refusal_rate": outcome_counts.get("refused", 0) / total_resolved,
        "timeout_rate": outcome_counts.get("timeout", 0) / total_resolved,
        "avg_match_score_accepted": avg_pertinence,
        "avg_proposals_per_request": avg_proposals_per_request,
        "raw_outcome_counts": outcome_counts,
    }


def get_recent_requests(conn, limit=50):
    with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
        cur.execute(
            """
            SELECT
                rl.request_id,
                rl.patient_id,
                rl.submitted_at,
                rl.extracted_at,
                rl.urgency_score,
                rl.urgency_scored_at,
                rl.closed_at,
                p.doctor_id,
                p.match_score,
                COALESCE(p.outcome, 'no_proposal_yet') AS outcome,
                COUNT(p_all.id) AS proposal_count,
                MIN(p_all.proposed_at) AS first_proposed_at
            FROM request_lifecycle rl
            LEFT JOIN LATERAL (
                SELECT doctor_id, match_score, outcome, proposed_at
                FROM proposals
                WHERE request_id = rl.request_id
                ORDER BY proposed_at DESC, id DESC
                LIMIT 1
            ) p ON TRUE
            LEFT JOIN proposals p_all ON p_all.request_id = rl.request_id
            GROUP BY
                rl.request_id,
                rl.patient_id,
                rl.submitted_at,
                rl.extracted_at,
                rl.urgency_score,
                rl.urgency_scored_at,
                rl.closed_at,
                p.doctor_id,
                p.match_score,
                p.outcome
            ORDER BY rl.submitted_at DESC NULLS LAST
            LIMIT %s
            """,
            (max(1, min(limit, 500)),),
        )
        return cur.fetchall()