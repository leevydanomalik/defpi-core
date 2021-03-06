/*-
 * #%L
 * dEF-Pi API
 * %%
 * Copyright (C) 2017 - 2018 Flexible Power Alliance Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.flexiblepower.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A PrivateNode is a piece of hardware in the dEF-Pi environment that only one specific user may deploy process on.
 *
 * @version 0.1
 * @since 20 mrt. 2017
 * @see UnidentifiedNode
 * @see PublicNode
 */
@Entity
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
public class PrivateNode extends Node {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId userId;

    /**
     * Create a PrivateNode from an {@link UnidentifiedNode} and a user to assign it to.
     *
     * @param unidentifiedNode The existing node which is unidentified, and should be upgraded to a private node
     * @param owner The intended private owner of the node
     */
    public PrivateNode(final UnidentifiedNode unidentifiedNode, final User owner) {
        super(unidentifiedNode.getDockerId(), unidentifiedNode.getHostname(), unidentifiedNode.getArchitecture());
        this.userId = owner.getId();
    }

}
