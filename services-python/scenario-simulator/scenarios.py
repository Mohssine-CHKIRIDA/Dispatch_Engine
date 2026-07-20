"""
6 fixed patient-request scenarios with ground truth, used to seed the whole
pipeline for testing/comparison (Phase 5: comparing S1-S4 strategies).

These are deliberately hardcoded (not randomly generated) so runs are
reproducible — matches the doc's "données simulées reproductibles (seed fixe)".
If you later want randomized/varied scenarios, add a seeded random generator
here instead, but keep a documented seed so results stay comparable across
strategy runs.

expected_urgency_score: 0 (non-urgent) to 3 (critical) — same scale as
IA2's request.urgency_score.payload.score, used by Evaluation to score
IA2's accuracy.

expected_specialty: free-text ground truth for what kind of professional
this request should be matched to. Used by Evaluation to score IA3's
matching quality (semantic similarity to the professional actually
proposed), NOT for exact string comparison — consistent with the doc's
"pas juste par tag exact" design goal for matching.
"""

from dataclasses import dataclass


@dataclass(frozen=True)
class Scenario:
    scenario_id: str
    patient_id: str
    text: str
    expected_urgency_score: int
    expected_specialty: str


SCENARIOS = [
    Scenario(
        scenario_id="s01_chest_pain_critical",
        patient_id="patient-sim-01",
        text=(
            "J'ai une douleur intense dans la poitrine depuis ce matin, "
            "j'ai du mal a respirer et je transpire beaucoup."
        ),
        expected_urgency_score=3,
        expected_specialty="cardiologue",
    ),
    Scenario(
        scenario_id="s02_mild_headache",
        patient_id="patient-sim-02",
        text="J'ai mal a la tete depuis deux jours, une douleur legere mais genante.",
        expected_urgency_score=1,
        expected_specialty="medecin generaliste",
    ),
    Scenario(
        scenario_id="s03_twisted_ankle",
        patient_id="patient-sim-03",
        text=(
            "Je me suis tordu la cheville en faisant du sport hier, "
            "ca gonfle un peu mais je peux marcher."
        ),
        expected_urgency_score=1,
        expected_specialty="kinesitherapeute",
    ),
    Scenario(
        scenario_id="s04_child_high_fever",
        patient_id="patient-sim-04",
        text=(
            "Mon enfant de 3 ans a une fievre tres elevee depuis cette nuit "
            "et respire difficilement, il est tres fatigue."
        ),
        expected_urgency_score=3,
        expected_specialty="pediatre",
    ),
    Scenario(
        scenario_id="s05_mild_skin_rash",
        patient_id="patient-sim-05",
        text="J'ai des petites rougeurs qui demangent legerement sur le bras depuis une semaine.",
        expected_urgency_score=0,
        expected_specialty="dermatologue",
    ),
    Scenario(
        scenario_id="s06_sudden_abdominal_pain",
        patient_id="patient-sim-06",
        text=(
            "J'ai une douleur soudaine et tres forte au ventre depuis une heure, "
            "je n'arrive plus a me tenir debout."
        ),
        expected_urgency_score=3,
        expected_specialty="medecin urgentiste",
    ),
]
