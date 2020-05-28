/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.0
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package io.rankone.rocsdk.embedded;

public class roc_embedded_array {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected roc_embedded_array(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(roc_embedded_array obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        rocJNI.delete_roc_embedded_array(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setElement_size(int value) {
    rocJNI.roc_embedded_array_element_size_set(swigCPtr, this, value);
  }

  public int getElement_size() {
    return rocJNI.roc_embedded_array_element_size_get(swigCPtr, this);
  }

  public void setSize(int value) {
    rocJNI.roc_embedded_array_size_set(swigCPtr, this, value);
  }

  public int getSize() {
    return rocJNI.roc_embedded_array_size_get(swigCPtr, this);
  }

  public void setData(String value) {
    rocJNI.roc_embedded_array_data_set(swigCPtr, this, value);
  }

  public String getData() {
    return rocJNI.roc_embedded_array_data_get(swigCPtr, this);
  }

  public roc_embedded_array() {
    this(rocJNI.new_roc_embedded_array(), true);
  }

}