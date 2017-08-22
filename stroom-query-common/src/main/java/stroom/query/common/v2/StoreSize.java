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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for describing the maximum number of items to hold in a store at each level of grouping.
 * e.g. 100,10,1 means hold 100 items at group level 0, 10 for each group level 1 and 1 for
 * each group level 2
 */
public class StoreSize {

    private final List<Integer> storeSizes;
    private final int defaultSize;

    public StoreSize(final List<Integer> storeSizes) {

        if (storeSizes != null) {
            this.storeSizes = new ArrayList<>(storeSizes);
        } else {
            this.storeSizes = Collections.emptyList();
        }

        if (this.storeSizes.size() > 0) {
            defaultSize = this.storeSizes.get(this.storeSizes.size() - 1);
        } else {
            defaultSize = Integer.MAX_VALUE;
        }
    }

    public int size(final int depth) {
        if (depth < storeSizes.size()) {
            return storeSizes.get(depth);
        }

        return defaultSize;
    }

    @Override
    public String toString() {
        return "StoreSize{" +
                "storeSizes=" + storeSizes +
                ", defaultSize=" + defaultSize +
                '}';
    }
}
