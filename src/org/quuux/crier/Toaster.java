/*
* Copyright (C) 2008 The Android Open Source Project
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

package org.quuux.crier;

import android.widget.Toast;
import android.content.Context;

public class Toaster
{
    private static Toast toast = null;

    public static void set(Toast t) {
	if(toast != null)
	    toast.cancel();

	toast = t;
	toast.show();
    }

    public static void cancel() {
	if(toast != null)
	    toast.cancel();
	
	toast = null;
    }

    public static void textShort(Context context, String text) {
	set(Toast.makeText(context, text, Toast.LENGTH_SHORT));
    }

    public static void textLong(Context context, String text) {
	set(Toast.makeText(context, text, Toast.LENGTH_LONG));
    }
}
