/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.rule.engine.metadata;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections.CollectionUtils;
import org.thingsboard.rule.engine.TbNodeUtils;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.TbNodeState;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;
import org.thingsboard.server.dao.relation.RelationService;

import java.util.List;

import static org.thingsboard.server.common.data.relation.RelationTypeGroup.COMMON;

public class TbGetRelatedAttributeNode extends TbEntityGetAttrNode<EntityId> {

    private TbGetRelatedAttrNodeConfiguration config;

    @Override
    public void init(TbNodeConfiguration configuration, TbNodeState state) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbGetRelatedAttrNodeConfiguration.class);
    }

    @Override
    protected ListenableFuture<EntityId> findEntityAsync(TbContext ctx, EntityId originator) {
        RelationService relationService = ctx.getRelationService();
        if (config.getDirection() == EntitySearchDirection.FROM) {
            ListenableFuture<List<EntityRelation>> asyncRelation = relationService.findByFromAndTypeAsync(originator, config.getRelationType(), COMMON);
            return Futures.transform(asyncRelation, (AsyncFunction<? super List<EntityRelation>, EntityId>)
                    r -> CollectionUtils.isNotEmpty(r) ? Futures.immediateFuture(r.get(0).getTo())
                            : Futures.immediateFailedFuture(new IllegalStateException("Relation not found")));
        } else if (config.getDirection() == EntitySearchDirection.TO) {
            ListenableFuture<List<EntityRelation>> asyncRelation = relationService.findByToAndTypeAsync(originator, config.getRelationType(), COMMON);
            return Futures.transform(asyncRelation, (AsyncFunction<? super List<EntityRelation>, EntityId>)
                    r -> CollectionUtils.isNotEmpty(r) ? Futures.immediateFuture(r.get(0).getFrom())
                            : Futures.immediateFailedFuture(new IllegalStateException("Relation not found")));
        }

        return Futures.immediateFailedFuture(new IllegalStateException("Unknown direction"));
    }
}
