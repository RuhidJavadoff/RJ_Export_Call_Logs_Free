package com.ruhidjavadoff.rjexportcalllogsfree

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ruhidjavadoff.rjexportcalllogsfree.databinding.ItemLanguageBinding

class LanguageAdapter(
    private var languages: List<AppLanguage>, // val -> var olaraq dəyişdirildi
    private var selectedLanguageCode: String,
    private val onLanguageSelected: (AppLanguage) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(languageItem: AppLanguage) {
            val displayText = if (languageItem.code == "system") {
                languageItem.nativeName
            } else {
                if (!languageItem.englishName.isNullOrBlank() && languageItem.nativeName != languageItem.englishName) {
                    "${languageItem.nativeName} (${languageItem.englishName})"
                } else {
                    languageItem.nativeName
                }
            }
            binding.textViewLanguageName.text = displayText
            binding.radioButtonLanguage.isChecked = (languageItem.code == selectedLanguageCode)
            binding.imageViewLanguageIcon.setImageResource(R.drawable.ic_language_default)

            binding.root.setOnClickListener {
                if (selectedLanguageCode != languageItem.code) {
                    // Activity-yə seçimi bildir, qalanını Activity idarə etsin
                    onLanguageSelected(languageItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languages[position])
    }

    override fun getItemCount(): Int = languages.size

    // Yeni metod: Adapterin məlumatlarını və seçilmiş dili yeniləmək üçün
    fun updateLanguages(newLanguages: List<AppLanguage>, newSelectedLanguageCode: String) {
        languages = newLanguages
        selectedLanguageCode = newSelectedLanguageCode
        notifyDataSetChanged() // Bütün siyahının yeniləndiyini bildir
    }

    // Mövcud seçimi yeniləmək üçün (yalnız radio button-lar üçün)
    fun updateSelection(newSelectedLanguageCode: String) {
        if (selectedLanguageCode != newSelectedLanguageCode) {
            val oldSelectedCode = selectedLanguageCode
            selectedLanguageCode = newSelectedLanguageCode

            val oldPosition = languages.indexOfFirst { it.code == oldSelectedCode }
            val newPosition = languages.indexOfFirst { it.code == newSelectedLanguageCode }

            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }
    }
}