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
<@pp.dropOutputFile />



<#list nullcheck.types as nullcheck>
<#if nullcheck.major == "Fixed">

<@pp.changeOutputFile name="/com/dremio/extras/plugins/kdb/exec/gnullCheck/${nullcheck.name}NullCheck.java" />

<#include "/@includes/license.ftl" />

package com.dremio.extras.plugins.kdb.exec.gnullCheck;

import java.io.IOException;

import com.dremio.exec.vector.complex.fn.ArrowBufInputStream;
import com.dremio.extras.plugins.kdb.exec.nullCheck.NullCheck;

/**
 * scan kdb buffer for nulls
 */
public class ${nullcheck.name}NullCheck implements NullCheck {
    @Override
    public boolean check(ArrowBufInputStream buf) throws IOException {
        ${nullcheck.native} l = buf.read${nullcheck.from}();
        return l != <#if nullcheck.shortval>${nullcheck.shortvalname}<#else>${nullcheck.from}</#if>.${nullcheck.check};
    }
}

</#if> <#-- nullcheck.major -->
</#list>