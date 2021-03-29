package c0d3.vitreen.app.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import c0d3.vitreen.app.R

class TopView(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    init {

        inflate(context, R.layout.top_view, this)

        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopView, 0, 0)
            val title = styledAttributes.getString(R.styleable.TopView_title)
            val subtitle = styledAttributes.getString(R.styleable.TopView_subtitle)
            val icon = styledAttributes.getDrawable(R.styleable.TopView_icon)

            val textView2 = findViewById<TextView>(R.id.textView2)
            textView2.text = title

            val textView = findViewById<TextView>(R.id.textView)
            textView.text = subtitle

            val imageView = findViewById<ImageView>(R.id.imageView)
            imageView.setImageDrawable(icon)

            styledAttributes.recycle()
        }

    }

}