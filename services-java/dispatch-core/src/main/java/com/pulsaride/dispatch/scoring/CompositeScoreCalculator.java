package com.pulsaride.dispatch.scoring;

import com.pulsaride.dispatch.domain.Doctor;
import org.springframework.stereotype.Component;

/**
 * score_composite(pro, cas), per section 5.3 of the v2 architecture
 * doc (supersedes the earlier 5-term formula from the informal
 * matching-algorithm doc):
 *
 * score_composite =
 * sim_cosinus(embedding_cas, embedding_pro) x 0.50 // affinite semantique
 * + disponibilite(pro) x 0.30 // AVAILABLE = 1, sinon 0
 * + (1 - taux_charge(pro)) x 0.20 // 0 consultations/h = 1
 *
 */
@Component
public class CompositeScoreCalculator {

    private static final double WEIGHT_SIMILARITY = 0.50;
    private static final double WEIGHT_AVAILABILITY = 0.30;
    private static final double WEIGHT_LOAD = 0.20;

    public double score(Doctor doctor, double similarity) {
        double availability = doctor.available() ? 1.0 : 0.0;

        return WEIGHT_SIMILARITY * similarity
                + WEIGHT_AVAILABILITY * availability
                + WEIGHT_LOAD * (1.0 - doctor.load());
    }
}
