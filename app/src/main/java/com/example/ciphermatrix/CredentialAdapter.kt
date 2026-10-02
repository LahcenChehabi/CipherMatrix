package com.example.ciphermatrix

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

class CredentialAdapter(
    private var list: List<Credential>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CredentialAdapter.ViewHolder>() {

    private val expandedStates = SparseBooleanArray()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAcc: TextView = view.findViewById(R.id.tv_account_name)
        val tvPass: TextView = view.findViewById(R.id.tv_password)
        val btnShow: ImageButton = view.findViewById(R.id.btn_show_hide)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
        val btnCopy: ImageButton = view.findViewById(R.id.btn_copy)
        val ivLogo: ImageView = view.findViewById(R.id.iv_account_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_credential, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvAcc.text = item.acc

        // Smart and comprehensive icon recognition system
        val accountName = item.acc.lowercase().trim()

        // Clear default tint to display downloaded icons with their original colors
        holder.ivLogo.imageTintList = null

        when {
            accountName.contains("facebook") -> holder.ivLogo.setImageResource(R.drawable.ic_facebook)
            accountName.contains("youtube") -> holder.ivLogo.setImageResource(R.drawable.ic_youtube)
            accountName.contains("gmail") -> holder.ivLogo.setImageResource(R.drawable.ic_gmail)
            accountName.contains("google") -> holder.ivLogo.setImageResource(R.drawable.ic_google)
            accountName.contains("chrome") -> holder.ivLogo.setImageResource(R.drawable.ic_chrome)
            accountName.contains("instagram") -> holder.ivLogo.setImageResource(R.drawable.ic_instagram)
            accountName.contains("spotify") -> holder.ivLogo.setImageResource(R.drawable.ic_spotify)
            accountName.contains("netflix") -> holder.ivLogo.setImageResource(R.drawable.ic_netflix)
            accountName.contains("whatsapp") -> holder.ivLogo.setImageResource(R.drawable.ic_whatsapp)
            accountName.contains("telegram") -> holder.ivLogo.setImageResource(R.drawable.ic_telegram)
            accountName.contains("linkedin") -> holder.ivLogo.setImageResource(R.drawable.ic_linkedin)
            accountName.contains("github") -> holder.ivLogo.setImageResource(R.drawable.ic_github)
            accountName.contains("wechat") -> holder.ivLogo.setImageResource(R.drawable.ic_wechat)

            // Keyword matching for banking and wallet accounts
            accountName.contains("bank") || accountName.contains("credit") -> holder.ivLogo.setImageResource(R.drawable.ic_bank_cards)
            accountName.contains("wallet") || accountName.contains("carte") -> holder.ivLogo.setImageResource(R.drawable.ic_card_wallet)

            // Keyword matching for email protection and system credentials
            accountName.contains("mail") || accountName.contains("protection") -> holder.ivLogo.setImageResource(R.drawable.ic_email_protection)
            accountName.contains("code") || accountName.contains("pass") -> holder.ivLogo.setImageResource(R.drawable.ic_password)

            else -> {
                // Fallback default icon and cyber green color tint for unrecognized accounts
                holder.ivLogo.setImageResource(android.R.drawable.ic_menu_compass)
                holder.ivLogo.imageTintList = ColorStateList.valueOf(Color.parseColor("#00E676"))
            }
        }

        // Initial decryption and password mask display logic
        val isExpanded = expandedStates.get(position, false)
        holder.tvPass.text = if (isExpanded) CryptoHelper.decrypt(item.pass) else "••••••••"

        // 🔒 Show/Hide button protected by biometric authentication and position fixes
        holder.btnShow.setOnClickListener { view ->
            val context = view.context

            // Get the exact live position of the item to prevent index mismatches
            val livePosition = holder.bindingAdapterPosition
            if (livePosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val isExpandedNow = expandedStates.get(livePosition, false)

            if (isExpandedNow) {
                // If already expanded, hide it directly without requesting biometrics
                expandedStates.put(livePosition, false)
                notifyItemChanged(livePosition)
            } else {
                // If hidden, request biometric authentication to reveal it
                if (context is FragmentActivity) {
                    val executor = ContextCompat.getMainExecutor(context)

                    val biometricPrompt = BiometricPrompt(context, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)

                                // Fetch live position inside the callback to ensure 100% thread safety
                                val callbackPos = holder.bindingAdapterPosition
                                if (callbackPos != RecyclerView.NO_POSITION) {
                                    expandedStates.put(callbackPos, true)
                                    notifyItemChanged(callbackPos)
                                }
                            }

                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                Toast.makeText(context, "Error: $errString", Toast.LENGTH_SHORT).show()
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Toast.makeText(context, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        })

                    // Custom text configurations for the biometric dialog system prompt
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Verify Identity")
                        .setSubtitle("Please use your fingerprint to show the password")
                        .setNegativeButtonText("Cancel")
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                }
            }
        }

        // Copy button logic fixed against position errors
        holder.btnCopy.setOnClickListener {
            val livePosition = holder.bindingAdapterPosition
            if (livePosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val currentItem = list[livePosition]
            val password = CryptoHelper.decrypt(currentItem.pass)
            val clipboard = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(it.context, "Password copied!", Toast.LENGTH_SHORT).show()
        }

        // Delete button logic fixed against position errors
        holder.btnDelete.setOnClickListener {
            val livePosition = holder.bindingAdapterPosition
            if (livePosition == RecyclerView.NO_POSITION) return@setOnClickListener

            onDeleteClick(list[livePosition].id)
        }
    }

    override fun getItemCount() = list.size

    // Live update function triggered during real-time filtering (Live Search)
    fun updateList(newList: List<Credential>) {
        this.list = newList
        notifyDataSetChanged()
    }
}