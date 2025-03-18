/*
 * =========================================================================
 *                                                                          
 *   Copyright (c) 2019-2025 Aquen (https://aquen.io)                  
 *                                                                          
 *   Licensed under the Apache License, Version 2.0 (the "License");        
 *   you may not use this file except in compliance with the License.       
 *   You may obtain a copy of the License at                                
 *                                                                          
 *       http://www.apache.org/licenses/LICENSE-2.0                         
 *                                                                          
 *   Unless required by applicable law or agreed to in writing, software    
 *   distributed under the License is distributed on an "AS IS" BASIS,      
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or        
 *   implied. See the License for the specific language governing           
 *   permissions and limitations under the License.                         
 *                                                                          
 * =========================================================================
 */
package io.aquen.atomichash;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

final class Node implements Serializable {

    private static final long serialVersionUID = 5892440116260222326L;

    final int byteMap;
    final Object[] values;


    Node(final int byteMap, final Object[] values) {
        super();
        this.byteMap = byteMap;
        this.values = values;
    }





    /**
     * Equivalent to Objects.equals(), but by being called only from
     * HashEntry we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }


}
