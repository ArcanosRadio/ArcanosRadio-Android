package de.developercity.arcanosradio.views.custom;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import de.developercity.arcanosradio.views.extensions.MoveUpwardBehavior;

/***
 * Custom relative layout, defined to enable the layout to react to
 * SnackBar appearance. Usually this will be the immediate child of
 * a CoordinatorLayout, and all other views shall be nested under this.
 */
@CoordinatorLayout.DefaultBehavior(MoveUpwardBehavior.class)
public class CustomRelativeLayout extends RelativeLayout {
    public CustomRelativeLayout(Context context) {
        super(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}