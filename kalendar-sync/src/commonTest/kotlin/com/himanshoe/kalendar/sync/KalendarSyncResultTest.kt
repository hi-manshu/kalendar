/*
 *
 *  * Copyright 2026 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package com.himanshoe.kalendar.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KalendarSyncResultTest {

    @Test
    fun getOrNull_success() {
        val result: KalendarSyncResult<Int> = KalendarSyncResult.Success(42)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun getOrNull_errors() {
        assertNull(KalendarSyncResult.PermissionDenied.getOrNull())
        assertNull(KalendarSyncResult.NotSupported.getOrNull())
        assertNull(KalendarSyncResult.Error("fail").getOrNull())
    }

    @Test
    fun getOrThrow_success() {
        val result: KalendarSyncResult<String> = KalendarSyncResult.Success("ok")
        assertEquals("ok", result.getOrThrow())
    }

    @Test
    fun getOrThrow_error_throws() {
        assertFailsWith<RuntimeException> {
            KalendarSyncResult.Error("boom").getOrThrow()
        }
        assertFailsWith<KalendarPermissionDeniedException> {
            KalendarSyncResult.PermissionDenied.getOrThrow()
        }
        assertFailsWith<UnsupportedOperationException> {
            KalendarSyncResult.NotSupported.getOrThrow()
        }
    }

    @Test
    fun onSuccess_invokesOnlyForSuccess() {
        var seen = 0
        KalendarSyncResult.Success(1).onSuccess { seen = it }
        assertEquals(1, seen)
        KalendarSyncResult.Error("x").onSuccess { seen = 99 }
        assertEquals(1, seen)
    }

    @Test
    fun onError_onPermissionDenied_onNotSupported() {
        var err: String? = null
        var denied = false
        var unsupported = false
        KalendarSyncResult.Error("bad").onError { message, _ -> err = message }
        KalendarSyncResult.PermissionDenied.onPermissionDenied { denied = true }
        KalendarSyncResult.NotSupported.onNotSupported { unsupported = true }
        assertEquals("bad", err)
        assertTrue(denied)
        assertTrue(unsupported)
    }
}
