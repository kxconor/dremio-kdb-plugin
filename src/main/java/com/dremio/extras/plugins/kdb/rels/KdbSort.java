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
package com.dremio.extras.plugins.kdb.rels;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;

import com.dremio.common.expression.SchemaPath;
import com.dremio.exec.expr.fn.FunctionLookupContext;
import com.dremio.exec.physical.base.PhysicalOperator;
import com.dremio.exec.physical.config.ExternalSort;
import com.dremio.exec.planner.common.MoreRelOptUtil;
import com.dremio.exec.planner.common.SortRelBase;
import com.dremio.exec.planner.physical.PhysicalPlanCreator;
import com.dremio.exec.planner.physical.Prel;
import com.dremio.exec.planner.physical.PrelUtil;
import com.dremio.exec.planner.physical.SortPrel;
import com.dremio.exec.planner.physical.visitor.PrelVisitor;
import com.dremio.exec.record.BatchSchema;
import com.dremio.extras.plugins.kdb.rels.translate.KdbPrelVisitor;
import com.dremio.extras.plugins.kdb.rels.translate.KdbQueryParameters;

/**
 * Implementation of {@link Sort}
 * relational expression in kdb.
 */
public class KdbSort extends SortRelBase implements KdbPrel, KdbTerminalPrel {

    private final List<SchemaPath> projectedColumns;
    private final BatchSchema tableMetadata;

    public KdbSort(RelOptCluster cluster, RelTraitSet traitSet,
                   RelNode child, RelCollation collation, RexNode offset, RexNode fetch,
                   BatchSchema tableMetadata,
                   List<SchemaPath> projectedColumns) {
        super(cluster, traitSet, child, collation, offset, fetch);
        this.tableMetadata = tableMetadata;
        this.projectedColumns = projectedColumns;
        assert getConvention() == child.getConvention();
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner,
                                      RelMetadataQuery mq) {
        return super.computeSelfCost(planner, mq).multiplyBy(0.05);
    }

    @Override
    public Sort copy(RelTraitSet traitSet, RelNode input,
                         RelCollation newCollation, RexNode offset, RexNode fetch) {
        return new KdbSort(getCluster(), traitSet, input, collation, offset,
                fetch, tableMetadata,
                projectedColumns);
    }

    @Override
    public PhysicalOperator getPhysicalOperator(PhysicalPlanCreator creator) throws IOException {
        Prel child = (Prel)this.getInput();
        PhysicalOperator childPOP = child.getPhysicalOperator(creator);
        return new ExternalSort(
                creator.props(this, (String)null, childPOP.getProps().getSchema(), SortPrel.RESERVE, SortPrel.LIMIT)
                        .cloneWithBound(creator.getOptionManager().getOption(SortPrel.BOUNDED))
                        .cloneWithMemoryFactor(creator.getOptionManager().getOption(SortPrel.FACTOR))
                        .cloneWithMemoryExpensive(true), childPOP, PrelUtil.getOrdering(this.collation, this.getInput().getRowType()), false);
    }


    @Override
    public Iterator<Prel> iterator() {
        return PrelUtil.iter(getInputs());
    }

    @Override
    public BatchSchema.SelectionVectorMode[] getSupportedEncodings() {
        return BatchSchema.SelectionVectorMode.DEFAULT;
    }

    @Override
    public boolean needsFinalColumnReordering() {
        return true;
    }

    @Override
    public BatchSchema getSchema(FunctionLookupContext functionLookupContext) {
        final KdbPrel child = (KdbPrel) getInput().accept(new MoreRelOptUtil.SubsetRemover());
        final BatchSchema childSchema = child.getSchema(functionLookupContext);
        return childSchema;
    }

    @Override
    public List<SchemaPath> projectedColumns() {
        return projectedColumns;
    }

    @Override
    public KdbQueryParameters accept(
            KdbPrelVisitor logicalVisitor, KdbQueryParameters value) {
        return logicalVisitor.visitSort(this, value);
    }

    @Override
    public BatchSchema.SelectionVectorMode getEncoding() {
        return BatchSchema.SelectionVectorMode.DEFAULT[0];
    }

    @Override
    public <T, X, E extends Throwable> T accept(PrelVisitor<T, X, E> logicalVisitor, X value) throws E {
        return logicalVisitor.visitPrel(this, value);
    }
}

// End KdbSort.java
