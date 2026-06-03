package br.com.tcc.github_poc.metrics.support;

import br.com.tcc.github_poc.metrics.dto.InsightsMetricsResponse.CommitClassification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ConventionalCommitClassifier {

    private static final Pattern FEAT = Pattern.compile("^feat[:(].*", Pattern.CASE_INSENSITIVE);
    private static final Pattern FIX  = Pattern.compile("^fix[:(].*",  Pattern.CASE_INSENSITIVE);

    public CommitClassification classify(List<String> headlines) {
        long feat = 0, fix = 0, other = 0;

        for (String h : headlines) {
            if (h == null || h.isBlank()) { other++; continue; }
            if (FEAT.matcher(h).matches())      feat++;
            else if (FIX.matcher(h).matches())  fix++;
            else                                other++;
        }

        long conventional = feat + fix;
        Double featRatio = conventional > 0 ? (double) feat / conventional : null;
        Double fixRatio  = conventional > 0 ? (double) fix  / conventional : null;

        return new CommitClassification(feat, fix, other, conventional, featRatio, fixRatio);
    }
}
