/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.covidcertificate.wallet.transfercode.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ch.admin.bag.covidcertificate.eval.utils.DEFAULT_DISPLAY_DATE_TIME_FORMATTER
import ch.admin.bag.covidcertificate.eval.utils.prettyPrint
import ch.admin.bag.covidcertificate.wallet.R
import ch.admin.bag.covidcertificate.wallet.databinding.ViewTransferCodeBubbleBinding
import ch.admin.bag.covidcertificate.wallet.transfercode.model.PublicKeyAlgorithm
import ch.admin.bag.covidcertificate.wallet.transfercode.model.TransferCodeModel
import java.time.Instant
import java.time.temporal.ChronoUnit

class TransferCodeBubbleView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	companion object {
		private const val DATE_REPLACEMENT_STRING = "{DATE}"
		private const val DAYS_REPLACEMENT_STRING = "{DAYS}"
	}

	private val binding = ViewTransferCodeBubbleBinding.inflate(LayoutInflater.from(context), this)
	private val validityIcons = arrayOf(
		R.drawable.ic_expire_1,
		R.drawable.ic_expire_2,
		R.drawable.ic_expire_3,
		R.drawable.ic_expire_4,
		R.drawable.ic_expire_5,
		R.drawable.ic_expire_6,
		R.drawable.ic_expire_7,
	)

	private var viewState: TransferCodeBubbleState? = null
	private var transferCode: TransferCodeModel? = null

	init {
		clipToPadding = false
		setPaddingRelative(0, context.resources.getDimensionPixelSize(R.dimen.spacing_medium_large), 0, 0)

		if (isInEditMode) {
			val creation = Instant.now()
			val expiration = creation.plus(7, ChronoUnit.DAYS)
			setTransferCode(
				TransferCodeModel(
					"A2X56K7WP",
					"",
					PublicKeyAlgorithm.RSA2048,
					"",
					"",
					creation.toEpochMilli(),
					expiration.toEpochMilli()
				)
			)
			setState(TransferCodeBubbleState.Valid(false))
		}
	}

	fun setTransferCode(transferCode: TransferCodeModel) {
		this.transferCode = transferCode
		updateView()
	}

	fun setState(newState: TransferCodeBubbleState) {
		if (newState != viewState) {
			viewState = newState
			updateView()
		}
	}

	private fun updateView() {
		when (val viewState = viewState) {
			is TransferCodeBubbleState.Created -> showCreatedState()
			is TransferCodeBubbleState.Valid -> showValidState(viewState)
			is TransferCodeBubbleState.Expired -> showExpiredState(viewState)
		}
	}

	private fun showCreatedState() {
		val transferCode = transferCode ?: return

		binding.transferCodeStatusIcon.isVisible = true
		binding.transferCodeLoadingIndicator.isVisible = false
		binding.transferCodeValidity.isVisible = false
		binding.transferCodeTitle.isVisible = true
		binding.transferCodeValue.isVisible = true
		binding.transferCodeExpired.isVisible = false

		binding.transferCodeStatusIcon.setImageResource(R.drawable.ic_check_mark)
		binding.transferCodeValue.setTransferCode(transferCode.code)

		setIconAndTextColor(R.color.blue)
		setBubbleBackgroundColor(R.color.blueish)
		showCreationTimestamp()
	}

	private fun showValidState(state: TransferCodeBubbleState.Valid) {
		val transferCode = transferCode ?: return

		binding.transferCodeStatusIcon.isVisible = !state.isRefreshing
		binding.transferCodeLoadingIndicator.isVisible = state.isRefreshing
		binding.transferCodeValidity.isVisible = true
		binding.transferCodeTitle.isVisible = false
		binding.transferCodeValue.isVisible = true
		binding.transferCodeExpired.isVisible = false

		val daysUntilExpiration = transferCode.getDaysUntilExpiration()
		if (daysUntilExpiration > 1) {
			binding.transferCodeValidity.text = context.getString(R.string.wallet_transfer_code_expire_plural)
				.replace(DAYS_REPLACEMENT_STRING, daysUntilExpiration.toString())
		} else {
			binding.transferCodeValidity.text = context.getString(R.string.wallet_transfer_code_expire_singular)
		}

		if (!state.isRefreshing) {
			binding.transferCodeStatusIcon.setImageResource(validityIcons[daysUntilExpiration - 1])
		}

		binding.transferCodeValue.setTransferCode(transferCode.code)

		setIconAndTextColor(R.color.blue)
		setBubbleBackgroundColor(R.color.blueish)
		showCreationTimestamp()
	}

	private fun showExpiredState(state: TransferCodeBubbleState.Expired) {
		binding.transferCodeStatusIcon.isVisible = true
		binding.transferCodeLoadingIndicator.isVisible = false
		binding.transferCodeValidity.isVisible = false
		binding.transferCodeTitle.isVisible = false
		binding.transferCodeValue.isVisible = false
		binding.transferCodeExpired.isVisible = true

		binding.transferCodeStatusIcon.setImageResource(R.drawable.ic_info_outline)

		setIconAndTextColor(if (state.isHighlighted) R.color.red else R.color.blue)
		setBubbleBackgroundColor(if (state.isHighlighted) R.color.redish else R.color.blueish)
		showCreationTimestamp()
	}

	private fun setIconAndTextColor(@ColorRes colorId: Int) {
		val color = ContextCompat.getColor(context, colorId)
		binding.transferCodeStatusIcon.imageTintList = ColorStateList.valueOf(color)
		binding.transferCodeExpired.setTextColor(color)
	}

	private fun setBubbleBackgroundColor(@ColorRes colorId: Int) {
		val color = ContextCompat.getColor(context, colorId)
		binding.background.backgroundTintList = ColorStateList.valueOf(color)
	}

	private fun showCreationTimestamp() {
		transferCode?.let {
			val creationTimestamp = Instant.ofEpochMilli(it.creationTimestamp)
			binding.transferCodeCreationDatetime.text = context.getString(R.string.wallet_transfer_code_createdat)
				.replace(DATE_REPLACEMENT_STRING, creationTimestamp.prettyPrint(DEFAULT_DISPLAY_DATE_TIME_FORMATTER))
		}
	}

	sealed class TransferCodeBubbleState {
		object Created : TransferCodeBubbleState()
		data class Valid(val isRefreshing: Boolean) : TransferCodeBubbleState()
		data class Expired(val isHighlighted: Boolean) : TransferCodeBubbleState()
	}

}