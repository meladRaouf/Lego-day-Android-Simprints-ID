package com.simprints.id.activities.settings.fragments.moduleselection

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ChipClickListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ModuleChipHelper
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.extensions.hideKeyboard
import kotlinx.android.synthetic.main.fragment_module_selection.*
import org.jetbrains.anko.sdk27.coroutines.onEditorAction
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import javax.inject.Inject

class ModuleSelectionFragment(
    private val application: Application
) : Fragment(), ModuleSelectionListener, ChipClickListener {

    @Inject
    lateinit var viewModelFactory: ModuleViewModelFactory

    private val adapter by lazy { ModuleAdapter(listener = this) }

    private val chipHelper by lazy { ModuleChipHelper(requireContext(), listener = this) }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ModuleViewModel::class.java)
    }

    private var modules = emptyList<Module>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerView()
        application.component.inject(this)
        fetchData()
    }

    override fun onModuleSelected(module: Module) {
        updateSelectionIfPossible(module)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onChipClick(module: Module) {
        updateSelectionIfPossible(module)
    }

    private fun configureRecyclerView() {
        rvModules.adapter = adapter
        val context = requireContext()
        val dividerItemDecoration = DividerItemDecoration(context,
            DividerItemDecoration.VERTICAL).apply {
            val colour = ContextCompat.getColor(context, R.color.simprints_light_grey)
            setDrawable(ColorDrawable(colour))
        }
        rvModules.addItemDecoration(dividerItemDecoration)
    }

    private fun fetchData() {
        viewModel.getModules().observe(this, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules.getUnselected())
            configureSearchView()
            configureTextViewVisibility()
            displaySelectedModules()
            rvModules.requestFocus()
        })
    }

    private fun configureSearchView() {
        configureSearchViewEditText()
        val queryListener = ModuleSelectionQueryListener(modules.getUnselected())
        searchView.setOnQueryTextListener(queryListener)
        observeSearchResults(queryListener)
    }

    private fun configureSearchViewEditText() {
        val editText = requireActivity().findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        with(editText) {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.muli)
            observeSearchButton()
            observeFocus()
        }
    }

    private fun displaySelectedModules() {
        val displayedModuleNames = chipHelper.findSelectedModuleNames(chipGroup)

        modules.forEach { module ->
            val isModuleDisplayed = displayedModuleNames.contains(module.name)
            val isModuleSelected = module.isSelected

            when {
                isModuleSelected && !isModuleDisplayed -> addChipForModule(module)
                !isModuleSelected && isModuleDisplayed -> removeChipForModule(module)
            }
        }
    }

    private fun addChipForModule(selectedModule: Module) {
        chipHelper.addModuleChip(chipGroup, selectedModule)
    }

    private fun removeChipForModule(selectedModule: Module) {
        chipHelper.removeModuleChip(chipGroup, selectedModule)
    }

    private fun observeSearchResults(queryListener: ModuleSelectionQueryListener) {
        queryListener.searchResults.observe(this, Observer { searchResults ->
            adapter.submitList(searchResults)
            txtNoResults.visibility = if (searchResults.isEmpty()) VISIBLE else GONE
            rvModules.scrollToPosition(0)
        })
    }

    private fun updateSelectionIfPossible(lastModuleChanged: Module) {
        val maxSelectedModules = viewModel.getMaxNumberOfModules()

        val selectedModulesSize = modules.getSelected().size
        val noModulesSelected = lastModuleChanged.isSelected && selectedModulesSize == 1
        val tooManyModulesSelected = !lastModuleChanged.isSelected
            && selectedModulesSize == maxSelectedModules

        when {
            noModulesSelected -> notifyNoModulesSelected()
            tooManyModulesSelected -> notifyTooManyModulesSelected(maxSelectedModules)
            else -> handleModuleSelected(lastModuleChanged)
        }
    }

    private fun handleModuleSelected(lastModuleChanged: Module) {
        lastModuleChanged.isSelected = !lastModuleChanged.isSelected
        viewModel.updateModules(modules)
    }

    private fun notifyNoModulesSelected() {
        Toast.makeText(
            application,
            R.string.settings_no_modules_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun notifyTooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            application,
            application.getString(R.string.settings_too_many_modules_toast, maxAllowed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configureTextViewVisibility() {
        if (modules.none { it.isSelected }) {
            txtNoModulesSelected.visibility = VISIBLE
            txtSelectedModules.visibility = GONE
        } else {
            txtNoModulesSelected.visibility = GONE
            txtSelectedModules.visibility = VISIBLE
        }
    }

    private fun List<Module>.getSelected() = filter { it.isSelected }

    private fun List<Module>.getUnselected() = filter { !it.isSelected }

    private fun EditText.observeSearchButton() {
        onEditorAction { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                requireActivity().hideKeyboard()
                v?.clearFocus()
                rvModules.requestFocus()
            }
        }
    }

    private fun EditText.observeFocus() {
        onFocusChange { v, hasFocus ->
            (v as EditText).isCursorVisible = hasFocus
            if (!hasFocus)
                rvModules?.scrollToPosition(0)
            // The safe call above is necessary only when the 'up' action bar button is clicked
        }
    }

}