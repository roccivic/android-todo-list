package com.placella.todo;

import android.content.Context;
import android.view.*;
import android.widget.LinearLayout;

public class Util {
	public static void hr(LinearLayout parent, Context context) {
		View ruler = new View(context);
		ruler.setBackgroundColor(0x20000000);
		parent.addView(
			ruler,
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 1
			)
		);
	}
	public static void defaultMenuHandler(Context context, MenuItem i) {
		if (i.getItemId() == R.id.menu_about) {
			new Dialog_Message(context, R.string.about_text).show();
	    }
	}
}
