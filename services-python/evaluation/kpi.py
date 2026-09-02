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