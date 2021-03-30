package c0d3.vitreen.app.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
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

    private var icon: Int = R.drawable.bigicon_leaf
        set(value) {
            field = value
            iconView.setImageDrawable(resources.getDrawable(field))
        }

    init {
        inflate(context, R.layout.top_view, this)

        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopView, 0, 0)
            title = styledAttributes.getString(R.styleable.TopView_title).orEmpty()
            icon = styledAttributes.getResourceId(R.styleable.TopView_icon, R.drawable.bigicon_leaf)

            titleView.text = title
            iconView.setImageDrawable(resources.getDrawable(icon))

            styledAttributes.recycle()
        }
    }

    fun setAttributes(title: String, icon: Int) {
        this.title = title
        this.icon = icon
    }

}