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

    private var title: String = ""
        set(value) {
            field = value
            titleView.text = field
        }

    @DrawableRes private var icon: Int = R.drawable.bigicon_logo
        set(value) {
            field = value
            iconView.setImageDrawable(resources.getDrawable(field))
        }

    init {
        inflate(context, R.layout.top_view, this)

        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopView, 0, 0)
            title = styledAttributes.getString(R.styleable.TopView_title).orEmpty()
            icon = styledAttributes.getResourceId(R.styleable.TopView_icon, R.drawable.bigicon_logo)

            titleView.text = title
            iconView.setImageDrawable(resources.getDrawable(icon))

            styledAttributes.recycle()
        }
    }

    fun setAttributes(title: String, @DrawableRes icon: Int) {
        this.title = title
        this.icon = icon
    }

}