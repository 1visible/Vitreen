package c0d3.vitreen.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import c0d3.vitreen.app.R
import kotlinx.android.synthetic.main.top_view.view.*

@SuppressLint("UseCompatLoadingForDrawables")
class TopView(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    @DrawableRes private var icon: Int = R.drawable.icon_home
        set(value) {
            field = value
            iconView.setImageDrawable(resources.getDrawable(field))
        }

    init {
        inflate(context, R.layout.top_view, this)

        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopView, 0, 0)
            icon = styledAttributes.getResourceId(R.styleable.TopView_icon, R.drawable.icon_home)
            iconView.setImageDrawable(resources.getDrawable(icon))
            styledAttributes.recycle()
        }
    }
}