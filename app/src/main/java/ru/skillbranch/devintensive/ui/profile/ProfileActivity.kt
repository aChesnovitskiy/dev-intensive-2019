package ru.skillbranch.devintensive.ui.profile

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_profile.*
import ru.skillbranch.devintensive.R
import ru.skillbranch.devintensive.models.Profile
import ru.skillbranch.devintensive.utils.Utils
import ru.skillbranch.devintensive.viewmodels.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val IS_EDIT_MODE = "IS_EDIT_MODE"
    }

    private lateinit var viewModel: ProfileViewModel
    var isEditMode = false
    var isRepositoryValidate = true
    lateinit var viewFields: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews(savedInstanceState)
        initViewModel()
        Log.d("M_ProfileActivity", "onCreate")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_EDIT_MODE, isEditMode)
    }

    /* Initialize ViewModel */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        // Set observer for profile LiveData
        viewModel.getProfileData().observe(this, Observer { updateUI(it)})
        // Set observer for appTheme LiveData
        viewModel.getTheme().observe(this, Observer { updateTheme(it) })
    }

    /* Switch theme depending on mode obtained from the appTheme LiveData */
    private fun updateTheme(mode: Int) {
        Log.d("M_ProfileActivity", "updateTheme")
        // AppCompatDelegate helps bring Material Design goodness to pre Lollipop Android versions
        delegate.setLocalNightMode(mode)
    }

    /* Update data in Views depending on data from the profile LiveData */
    private fun updateUI(profile: Profile) {
        profile.toMap().also {
            for ((k, v) in viewFields) {
                v.text = it[k].toString()
            }
            val initials = Utils.toInitials(it["firstName"].toString(), it["lastName"].toString()) ?: ""
            if (!initials.isBlank()) {
            iv_avatar.setInitials(initials)
                /*by Neikist*/
//                val avatar = getAvatarBitmap(initials)
//                iv_avatar.setImageBitmap(avatar)
            } else {
                iv_avatar.setImageResource(R.drawable.avatar_default)
            }
        }

    }

//    private fun getAvatarBitmap(text: String): Bitmap {
//        val color = TypedValue()
//        theme.resolveAttribute(R.attr.colorAccent, color, true)
//
//        return TextBitmapBuilder(
//            iv_avatar.layoutParams.width,
//            iv_avatar.layoutParams.height
//        )
//            .setBackgroundColor(color.data)
//            .setText(text)
//            .setTextSize(this.spToPx(48))
//            .setTextColor(Color.WHITE)
//            .build()
//    }

    /* Initialize Activity UI and set listeners for "edit/save" and "switch theme" buttons
    and for et_repository text changed */
    private fun initViews(savedInstanceState: Bundle?) {
        // Create map of Views for easy work with them
        viewFields = mapOf(
            "nickName" to tv_nick_name,
            "rank" to tv_rank,
            "firstName" to et_first_name,
            "lastName" to et_last_name,
            "about" to et_about,
            "repository" to et_repository,
            "rating" to tv_rating,
            "respect" to tv_respect
        )

        isEditMode = savedInstanceState?.getBoolean(IS_EDIT_MODE, false) ?: false
        showCurrentMode(isEditMode)

        btn_edit.setOnClickListener {
            if (isEditMode) {
                if (!isRepositoryValidate) et_repository.setText("")
                saveProfileInfo()
            }
            isEditMode = !isEditMode
            showCurrentMode(isEditMode)
        }

        btn_switch_theme.setOnClickListener{
            viewModel.switchTheme()
        }

        et_repository.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                isRepositoryValidate = repositoryValidation(text)
                if (isRepositoryValidate || text.isEmpty()) {
                    wr_repository.isErrorEnabled = false
                    wr_repository.error = null
                } else {
                    wr_repository.isErrorEnabled = true
                    wr_repository.error = "Невалидный адрес репозитория"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    private fun repositoryValidation(text: String) : Boolean {
        val exceptions = "(enterprise)|(features)|(topics)|(collections)|(trending)|(events)|"+
                "(marketplace)|(pricing)|(nonprofit)|(customer-stories)|(security)|(login)|(join)"
        val regex = "^(https:\\/\\/)?(www\\.)?(github\\.com\\/)(?!($exceptions)"+
                "(?=\\/|\$))[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}(\\/)?\$"
        return text.matches(regex.toRegex())
    }

    /* Switch Activity UI depending on edit/show mode */
    private fun showCurrentMode(isEdit: Boolean) {
        // Get map of editable Views from map of all Views
        val info = viewFields.filter {
            setOf(
                "firstName",
                "lastName",
                "about",
                "repository"
            ).contains(it.key)
        }
        // Change view of editable Views
        for ((_, v) in info) {
            v as EditText
            v.isFocusable = isEdit
            v.isFocusableInTouchMode = isEdit
            v.isEnabled = isEdit
            v.background.alpha = if (isEdit) 255 else 0
        }

        ic_eye.visibility = if (isEdit) View.GONE else View.VISIBLE
        wr_about.isCounterEnabled = isEdit

        // Switch view of edit/save button
        with(btn_edit) {
            val filter: ColorFilter? = if (isEdit) {
                PorterDuffColorFilter(
                    resources.getColor(R.color.color_accent, theme),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                null
            }

            val icon = if (isEdit) {
                resources.getDrawable(R.drawable.ic_save_white_24dp, theme)
            } else {
                resources.getDrawable(R.drawable.ic_edit_black_24dp, theme)
            }

            background.colorFilter = filter
            setImageDrawable(icon)
        }
    }

    /* Get profile's data from editable Views and save it through the ViewModel */
    fun saveProfileInfo() {
        Profile(
            firstName = et_first_name.text.toString(),
            lastName = et_last_name.text.toString(),
            about = et_about.text.toString(),
            repository = et_repository.text.toString()
        ).apply {
            viewModel.saveProfileData(this)
        }
    }
}

