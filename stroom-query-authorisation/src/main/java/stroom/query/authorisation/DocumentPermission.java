/*
 * Copyright 2018 Crown Copyright
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

package stroom.query.authorisation;

/**
 * This known actions that require permission.
 */
public enum DocumentPermission {
    /**
     * Users with the use permission can use documents.
     */
    USE("Use"),

    /**
     * Users with the create permission can find and create/saveAs documents.
     */
    CREATE("Create"),

    /**
     * Users with the read permission can find, create and read/load/view
     * documents.
     */
    READ("Read"),

    /**
     * Users with the update permission can find, create, read and update/save
     * documents.
     */
    UPDATE("Update"),

    /**
     * Users with delete permission can find, create, read, update and delete
     * documents.
     */
    DELETE("Delete"),

    /**
     * Owners have permission to find, create, read, update and delete documents
     * plus they can change permissions.
     */
    OWNER("Owner"),

    /**
     * Users with import permission can import documents that they have create
     * and update permissions for.
     */
    IMPORT("Import"),

    /**
     * Users with export permission can export documents.
     */
    EXPORT("Export");
    
    private final String name;
    
    DocumentPermission(final String name) {
        this.name =name;
    }
    
    public String getName() {
        return this.name;
    }

    public String getTypedPermission(final String docType) {
        return this.getName() + " - " + docType;
    }
}
