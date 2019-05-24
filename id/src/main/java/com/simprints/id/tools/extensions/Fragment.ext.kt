package com.simprints.id.tools.extensions

import androidx.fragment.app.Fragment

fun Fragment.activityIsPresentAndFragmentIsAdded(): Boolean = activity?.let {
        !it.isFinishing && isAdded
    } ?: false



