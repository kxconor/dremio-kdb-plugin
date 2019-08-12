/*
 * Copyright (C) 2017-2019 UBS Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.extras.plugins.kdb;

import java.util.Set;

import org.apache.calcite.plan.RelOptRule;

import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.ops.OptimizerRulesContext;
import com.dremio.exec.planner.PlannerPhase;
import com.dremio.exec.store.StoragePluginRulesFactory;
import com.dremio.extras.plugins.kdb.rules.KdbAggregateAggregateRule;
import com.dremio.extras.plugins.kdb.rules.KdbAggregateRule;
import com.dremio.extras.plugins.kdb.rules.KdbFilterRule;
import com.dremio.extras.plugins.kdb.rules.KdbLimitRule;
import com.dremio.extras.plugins.kdb.rules.KdbProjectRule;
import com.dremio.extras.plugins.kdb.rules.KdbScanPrule;
import com.dremio.extras.plugins.kdb.rules.KdbScanRule;
import com.dremio.extras.plugins.kdb.rules.KdbSortRule;
import com.dremio.extras.plugins.kdb.rules.KdbTopNRule;
import com.google.common.collect.ImmutableSet;

/**
 * Rules definition for kdb query engine
 */
public class KdbRulesFactory extends StoragePluginRulesFactory.StoragePluginTypeRulesFactory {

    @Override
    public Set<RelOptRule> getRules(OptimizerRulesContext optimizerContext, PlannerPhase phase, SourceType pluginType) {
        switch (phase) {
            case LOGICAL: {
                ImmutableSet.Builder<RelOptRule> builder = ImmutableSet.builder();
                builder.add(new KdbScanRule(pluginType));
                return builder.build();
            }
            case PHYSICAL:
                ImmutableSet.Builder<RelOptRule> builder = ImmutableSet.builder();
                builder.add(KdbFilterRule.INSTANCE);
                builder.add(new KdbProjectRule(optimizerContext.getFunctionRegistry()));

//                builder.add(KdbSortRule.PHYSICAL_INSTANCE);
                builder.add(KdbLimitRule.INSTANCE);
                builder.add(KdbTopNRule.INSTANCE);
                builder.add(new KdbAggregateRule(optimizerContext.getFunctionRegistry()));
                builder.add(new KdbAggregateAggregateRule(optimizerContext.getFunctionRegistry()));
                builder.add(new KdbScanPrule(optimizerContext.getFunctionRegistry()));
                return builder.build();
            default:
                return ImmutableSet.of();
        }
    }
}
