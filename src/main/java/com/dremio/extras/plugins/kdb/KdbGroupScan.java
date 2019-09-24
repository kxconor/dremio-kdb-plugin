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

import java.util.ArrayList;
import java.util.List;

import com.dremio.common.exceptions.ExecutionSetupException;
import com.dremio.common.expression.SchemaPath;
import com.dremio.exec.physical.base.AbstractGroupScan;
import com.dremio.exec.physical.base.OpProps;
import com.dremio.exec.physical.base.SubScan;
import com.dremio.exec.record.BatchSchema;
import com.dremio.exec.store.SplitWork;
import com.dremio.exec.store.TableMetadata;
import com.dremio.extras.plugins.kdb.exec.KdbSubScan;
import com.dremio.service.namespace.dataset.proto.PartitionProtobuf;
import com.dremio.service.namespace.dataset.proto.ReadDefinition;

/**
 * scan of a kdb table...break into smaller for executors
 */
public class KdbGroupScan extends AbstractGroupScan {
    private final OpProps opProps;
    private final String sql;

    public KdbGroupScan(
            OpProps opProps,
            TableMetadata dataset,
            List<SchemaPath> columns,
            String sql) {
        super(opProps, dataset, columns);
        this.opProps = opProps;
        this.sql = sql;
    }

    @Override
    public SubScan getSpecificScan(List<SplitWork> work) throws ExecutionSetupException {
        List<PartitionProtobuf.DatasetSplit> splits = new ArrayList<>(work.size());
        BatchSchema schema = getDataset().getSchema();
        for (SplitWork split : work) {
            splits.add(split.getDatasetSplit());
        }

        final ReadDefinition readDefinition = dataset.getReadDefinition();

        return new KdbSubScan(opProps, splits, schema, dataset.getName().getPathComponents(), sql, dataset.getStoragePluginId(), columns,
                readDefinition.getExtendedProperty(), readDefinition.getPartitionColumnsList(), 10000);//todo
    }
    @Override
    public int getOperatorType() {
        return 73;
    }


}
