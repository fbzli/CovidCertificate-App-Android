package ch.admin.bag.covidcertificate.wallet.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ch.admin.bag.covidcertificate.eval.models.Bagdgc
import ch.admin.bag.covidcertificate.eval.models.CertType
import ch.admin.bag.covidcertificate.wallet.R

class CertificatesListViewHolder(
	itemView: View,
	private val onStartDragListener: ((CertificatesListViewHolder) -> Unit)?
) :
	RecyclerView.ViewHolder(itemView) {

	companion object {
		@SuppressLint("ClickableViewAccessibility")
		fun inflate(
			inflater: LayoutInflater,
			parent: ViewGroup,
			onStartDragListener: ((CertificatesListViewHolder) -> Unit)? = null
		): CertificatesListViewHolder {
			val itemView = inflater.inflate(R.layout.item_certificate_list, parent, false)
			val viewHolder = CertificatesListViewHolder(itemView, onStartDragListener)
			itemView.findViewById<View>(R.id.item_certificate_list_drag_handle).setOnTouchListener { v, event ->
				if (event.actionMasked == MotionEvent.ACTION_DOWN) {
					onStartDragListener?.invoke(viewHolder);
				}
				return@setOnTouchListener false
			}
			return viewHolder
		}
	}

	fun bindItem(verifiedCertificate: VerifiedCeritificateItem, onCertificateClickListener: ((Bagdgc) -> Unit)? = null) =
		verifiedCertificate.bindView(itemView, onCertificateClickListener)
}