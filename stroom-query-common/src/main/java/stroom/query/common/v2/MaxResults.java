/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.common.v2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for describing the maximum number of result rows requested by the user/client at each level of grouping.
 * e.g. 100,10,1 means hold 100 items at group level 0, 10 for each group level 1 and 1 for
 * each group level 2
 */
public class MaxResults {
    private final List<Integer> effectiveMaxResultsSizes;
    private final int defaultSize;

    public MaxResults(final List<Integer> userMaxResultsSizes, final List<Integer> defaultMaxResultsSizes) {
        effectiveMaxResultsSizes = getEffectiveSizes(userMaxResultsSizes, defaultMaxResultsSizes);

        if (effectiveMaxResultsSizes.size() > 0) {
            defaultSize = effectiveMaxResultsSizes.get(effectiveMaxResultsSizes.size() - 1);
        } else {
            defaultSize = Integer.MAX_VALUE;
        }
    }

    private List<Integer> getEffectiveSizes(final List<Integer> userMaxResultsSizes,
                                            final List<Integer> defaultMaxResultsSizes) {
        int size = 0;
        if (userMaxResultsSizes != null) {
            size = userMaxResultsSizes.size();
        }
        if (defaultMaxResultsSizes != null && defaultMaxResultsSizes.size() > size) {
            size = defaultMaxResultsSizes.size();
        }

        return IntStream.range(0, size)
                .mapToObj(i -> {
                    Integer val = Integer.MAX_VALUE;
                    if (defaultMaxResultsSizes != null && i < defaultMaxResultsSizes.size()) {
                        val = defaultMaxResultsSizes.get(i);
                    }
                    if (userMaxResultsSizes != null && i < userMaxResultsSizes.size()) {
                        final Integer userSize = userMaxResultsSizes.get(i);
                        if (userSize != null && userSize < val) {
                            val = userSize;
                        }
                    }
                    return val;
                })
                .collect(Collectors.toList());
    }

    public int size(final int depth) {
        if (depth < effectiveMaxResultsSizes.size()) {
            return effectiveMaxResultsSizes.get(depth);
        }
        return defaultSize;
    }

    @Override
    public String toString() {
        return "MaxResults{" +
                "effectiveMaxResultsSizes=" + effectiveMaxResultsSizes +
                ", defaultSize=" + defaultSize +
                '}';
    }
}
