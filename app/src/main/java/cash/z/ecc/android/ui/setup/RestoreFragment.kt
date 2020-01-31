package cash.z.ecc.android.ui.setup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentRestoreBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.toAppColor
import cash.z.ecc.android.ext.toPx
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.ext.ZcashSdk
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hootsuite.nachos.ChipConfiguration
import com.hootsuite.nachos.chip.ChipCreator
import com.hootsuite.nachos.chip.ChipSpan
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer
import com.tylersuehr.chips.Chip
import com.tylersuehr.chips.ChipsAdapter
import com.tylersuehr.chips.SeedWordAdapter
import kotlinx.coroutines.launch


class RestoreFragment : BaseFragment<FragmentRestoreBinding>(), View.OnKeyListener {

    private val walletSetup: WalletSetupViewModel by activityViewModel(false)

    private lateinit var seedWordRecycler: RecyclerView
    private var seedWordAdapter: SeedWordAdapter? = null

    override fun inflate(inflater: LayoutInflater): FragmentRestoreBinding =
        FragmentRestoreBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seedWordRecycler = binding.chipsInput.findViewById<RecyclerView>(R.id.chips_recycler)
        seedWordAdapter = SeedWordAdapter(seedWordRecycler.adapter as ChipsAdapter).onDataSetChanged {
            onChipsModified()
        }.also { onChipsModified() }
        seedWordRecycler.adapter = seedWordAdapter


        binding.chipsInput.apply {
            setFilterableChipList(getChips())
            setDelimiter("[ ;,]", true)
        }

        binding.buttonDone.setOnClickListener {
            onDone()
        }

        binding.buttonSuccess.setOnClickListener {
            onEnterWallet()
        }
//
//
//        seedWordAdapter!!.editText.setOnKeyListener(this)

        binding.textTitle.setOnClickListener {
            seedWordAdapter!!.editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_CLASS_NUMBER
        }

        binding.textSubtitle.setOnClickListener {
            seedWordAdapter!!.editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity?.onFragmentBackPressed(this) {
            if (seedWordAdapter == null || seedWordAdapter?.itemCount == 1) {
                onExit()
            } else {
                MaterialAlertDialogBuilder(activity)
                    .setMessage("Are you sure? For security, the words that you have entered will be cleared!")
                    .setTitle("Abort?")
                    .setPositiveButton("Stay") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        onExit()
                    }
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Require one less tap to enter the seed words
        touchScreenForUser()
    }


    private fun onExit() {
        hideAutoCompleteWords()
        setKeyboardShown(false)
        mainActivity?.navController?.popBackStack()
    }

    private fun onEnterWallet() {
        mainActivity?.navController?.navigate(R.id.action_nav_restore_to_nav_home)
    }

    private fun onDone() {
        setKeyboardShown(false)
        val seedPhrase = binding.chipsInput.selectedChips.joinToString(" ") {
            it.title
        }
        var birthday = binding.root.findViewById<TextView>(R.id.input_birthdate).text.toString()
            .let { birthdateString ->
                if (birthdateString.isNullOrEmpty()) ZcashSdk.SAPLING_ACTIVATION_HEIGHT else birthdateString.toInt()
            }.coerceAtLeast(ZcashSdk.SAPLING_ACTIVATION_HEIGHT)

        importWallet(seedPhrase, birthday)
    }

    private fun importWallet(seedPhrase: String, birthday: Int) {
        setKeyboardShown(false)
        mainActivity?.apply {
            lifecycleScope.launch {
                mainActivity?.startSync(walletSetup.importWallet(seedPhrase, birthday))
            }
            playSound("sound_receive_small.mp3")
            vibrateSuccess()
        }

        binding.groupDone.visibility = View.GONE
        binding.groupStart.visibility = View.GONE
        binding.groupSuccess.visibility = View.VISIBLE
    }

    private fun onChipsModified() {
        seedWordAdapter?.editText?.apply {
            postDelayed({
                requestFocus()
                isCursorVisible = false
            },40L)
        }
        setDoneEnabled()
    }

    private fun setDoneEnabled() {
        val count = seedWordAdapter?.itemCount ?: 0
        binding.groupDone.goneIf(count <= 24)
    }

    private fun setKeyboardShown(isShown: Boolean) {
        if (isShown) {
            mainActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        } else {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view!!.windowToken, 0);
        }
    }

    private fun hideAutoCompleteWords() {
        seedWordAdapter?.editText?.setText("")
    }

    private fun getChips(): List<Chip> {
        return resources.getStringArray(R.array.word_list).map {
            SeedWordChip(it)
        }
    }

    private fun touchScreenForUser() {
        seedWordAdapter?.editText?.apply {
            postDelayed({
                seedWordAdapter?.editText?.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                dispatchTouchEvent(motionEvent(ACTION_DOWN))
                dispatchTouchEvent(motionEvent(ACTION_UP))
            }, 100L)
        }
    }

    private fun motionEvent(action: Int) = SystemClock.uptimeMillis().let { now ->
        MotionEvent.obtain(now, now, action, 0f, 0f, 0)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

}

class SeedWordTokenizer(context: Context) : SpanChipTokenizer<ChipSpan>(context, MyChipSpanChipCreator(), ChipSpan::class.java) {
    override fun applyConfiguration(text: Editable, chipConfiguration: ChipConfiguration) {
        mChipConfiguration = chipConfiguration


        val allChips = findAllChips(0, text.length, text)
        allChips.forEachIndexed { i, chip ->
            val chipStart = findChipStart(chip, text)
            deleteChip(chip, text)
            val newChip = (mChipCreator as MyChipSpanChipCreator).createChip(mContext, chip, i)
            text.insert(chipStart, terminateToken(newChip))
        }
    }

}

class MyChipSpanChipCreator : ChipCreator<ChipSpan> {
    override fun createChip(context: Context, text: CharSequence, data: Any?): ChipSpan {
        return MyChipSpan(context, text, ContextCompat.getDrawable(context, R.mipmap.ic_launcher), data)
    }

    fun createChip(context: Context, existingChip: ChipSpan, data: Any?): MyChipSpan {
        return MyChipSpan(context, existingChip, data)
    }

    override fun createChip(context: Context, existingChip: ChipSpan): ChipSpan {
        throw IllegalAccessException("Provide data when creating a chip")
    }

    override fun configureChip(chip: ChipSpan, chipConfiguration: ChipConfiguration) {
        val chipHorizontalSpacing = chipConfiguration.chipHorizontalSpacing
        val chipBackground = chipConfiguration.chipBackground
        val chipCornerRadius = chipConfiguration.chipCornerRadius
        val chipTextColor = chipConfiguration.chipTextColor
        val chipTextSize = chipConfiguration.chipTextSize
        val chipHeight = chipConfiguration.chipHeight
        val chipVerticalSpacing = chipConfiguration.chipVerticalSpacing
        val maxAvailableWidth = chipConfiguration.maxAvailableWidth

        if (chipHorizontalSpacing != -1) {
            chip.setLeftMargin(chipHorizontalSpacing / 2)
            chip.setRightMargin(chipHorizontalSpacing / 2)
        }
        if (chipBackground != null) {
            chip.setBackgroundColor(chipBackground)
        }
        if (chipCornerRadius != -1) {
            chip.setCornerRadius(chipCornerRadius)
        }
        if (chipTextColor != Color.TRANSPARENT) {
            chip.setTextColor(chipTextColor)
        }
        if (chipTextSize != -1) {
            chip.setTextSize(chipTextSize)
        }
        if (chipHeight != -1) {
            chip.setChipHeight(chipHeight)
        }
        if (chipVerticalSpacing != -1) {
            chip.setChipVerticalSpacing(chipVerticalSpacing)
        }
        if (maxAvailableWidth != -1) {
            chip.setMaxAvailableWidth(maxAvailableWidth)
        }

        chip.setShowIconOnLeft(true)
    }
}

class MyChipSpan : ChipSpan {
    val index: Int

    constructor(context: Context, text: CharSequence, drawable: Drawable?, data: Any?)
            : super(context, text, drawable, data) {
        index = data as? Int ?: 0
    }

    constructor(context: Context, chip: ChipSpan, data: Any?)
            : super(context, chip) {
        index = data as? Int ?: 0
    }

    override fun drawBackground(canvas: Canvas, x: Float, top: Int, bottom: Int, paint: Paint) {
        val rect = RectF(x, top.toFloat(), x + mChipWidth, bottom.toFloat())
        val cornerRadius = 4.0f.toPx()

        paint.color = R.color.background_banner.toAppColor()
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        paint.color = R.color.background_banner_stroke.toAppColor()
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 10.0f
        paint.strokeWidth = 1.5f.toPx()

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        paint.style = Paint.Style.FILL
        paint.color = mTextColor
    }
}

class SeedWordChip(val word: String, var index: Int = -1) : Chip() {
    override fun getSubtitle(): String? = null//"subtitle for $word"
    override fun getAvatarDrawable(): Drawable? = null
    override fun getId() = index
    override fun getTitle() = word
    override fun getAvatarUri() = null
}