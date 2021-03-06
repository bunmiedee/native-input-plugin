package com.appgyver.plugins.nativeinput;

import com.appgyver.ui.AGStyler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Native Input Plugin main class.
 */
public class NativeInput extends CordovaPlugin {

    private static final String TAG = "NativeInput";

    private static final String RIGHT = "right";

    static final int AUTO_CLOSE_KEYBOARD = 0;

    static final int PANEL_ARG = 0;

    static final int TEXT_ARG = 0;

    static final int INPUT_ARG = 1;

    static final int LEFT_BUTTON_ARG = 2;

    static final int RIGHT_BUTTON_ARG = 3;

    static final int BUTTON_WIDTH = 230;

    static final int BUTTON_HEIGHT = 60;

    private static final String SHOW = "show";

    private static final String SETUP = "setup";

    private static final String HIDE = "hide";

    private static final String ON_CHANGE = "onChange";

    private static final String ON_KEYBOARD_CLOSE = "onKeyboardClose";

    private static final String ON_KEYBOARD_ACTION = "onKeyboardAction";

    private static final String GET_VALUE = "getValue";

    private static final String CLOSE_KEYBOARD = "closeKeyboard";

    private static final String ON_BUTTON_ACTION = "onButtonAction";

    private static final String STYLE_CLASS = "styleClass";

    private static final String NATIVE_INPUT_PANEL = "nativeInput-panel";

    private static final String STYLE_ID = "styleId";

    private static final String STYLE_CSS = "styleCSS";

    private static final String NATIVE_INPUT = "nativeInput";

    private static final String PLACE_HOLDER = "placeHolder";

    private static final String NATIVE_INPUT_RIGHT_BUTTON = "nativeInput-rightButton";

    private static final String NATIVE_INPUT_LEFT_BUTTON = "nativeInput-leftButton";

    private static final String LEFT = "left";

    private static final String LINES = "lines";

    private static final String TYPE = "type";

    private static final String URI = "uri";

    private static final String EMAIL = "email";

    private static final String NUMBER = "number";

    private static final String DONE = "done";

    private static final String GO = "go";

    private static final String NEXT = "next";

    private static final String SEND = "send";

    private static final String NEWLINE = "newline";

    private CallbackContext mOnChangeCallback;

    private CallbackContext mOnKeyboardActionCallback;

    private CallbackContext mOnKeyboardCloseCallback;

    private CallbackContext mOnButtonActionCallback;

    private CustomEditText mEditText;

    private LinearLayout mPanel;

    private Button mLeftButton;

    private Button mRightButton;

    private boolean mAutoCloseKeyboard;

    static boolean sPixateInitiated = false;

    private TextWatcher mTextChangedListener = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            if (mOnChangeCallback != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                        getValue());
                pluginResult.setKeepCallback(true);
                mOnChangeCallback.sendPluginResult(pluginResult);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
    };

    private TextView.OnEditorActionListener mKeyboardActionListener
            = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            boolean isNewLineAction = actionId == EditorInfo.IME_ACTION_UNSPECIFIED;

            String action = getActionName(actionId);

            if (mOnKeyboardActionCallback != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, action);
                pluginResult.setKeepCallback(true);
                mOnKeyboardActionCallback.sendPluginResult(pluginResult);
            }
            if (mAutoCloseKeyboard && !isNewLineAction) {
                closeKeyboard();
            }
            return false;
        }
    };


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        setupPixate();
    }

    private void createBasicUi() {
        mPanel = new LinearLayout(webView.getContext());
        mPanel.setOrientation(LinearLayout.HORIZONTAL);
        AGStyler.setStyleClass(mPanel, NATIVE_INPUT_PANEL);

        mEditText = new CustomEditText(webView.getContext()){
            @Override
            public boolean onKeyPreIme(int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (mOnKeyboardCloseCallback != null) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                        pluginResult.setKeepCallback(true);
                        mOnKeyboardCloseCallback.sendPluginResult(pluginResult);
                    }
                    return false;
                }
                return super.onKeyPreIme(keyCode, event);
            }
        };

        AGStyler.setStyleClass(mEditText, NATIVE_INPUT);

        mEditText.setPadding(8, 4, 8, 8);

        mEditText.addTextChangedListener(mTextChangedListener);

        mEditText.setOnEditorActionListener(mKeyboardActionListener);
    }

    private void addEditTextToPanel(boolean hasBothButtons) {
        mPanel.removeView(mEditText);

        float weight = hasBothButtons ? 2f : 1f;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                weight);
        params.leftMargin = 10;
        params.rightMargin = 10;
        params.topMargin = 6;
        params.bottomMargin = 10;

        mPanel.addView(mEditText, params);
    }

    public boolean execute(final String action, final JSONArray args,
            final CallbackContext callbackContext) throws JSONException {

        if (action.equals(SETUP)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setup(callbackContext, args);
                    } catch (JSONException e) {
                        callbackContext.error("Invalid JSON parameter - error: " + e.getMessage());
                    }
                }
            });
        } else if (action.equals(SHOW)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        show(callbackContext, args);
                    } catch (JSONException e) {
                        callbackContext.error("Invalid JSON parameter - error: " + e.getMessage());
                    }
                }
            });
        } else if (action.equals(HIDE)) {
            hide(callbackContext);
        } else if (action.equals(ON_CHANGE)) {
            onChange(callbackContext);
        } else if (action.equals(ON_KEYBOARD_ACTION)) {
            onKeyboardAction(callbackContext, args);
        } else if (action.equals(ON_KEYBOARD_CLOSE)) {
            onKeyboardClose(callbackContext, args);
        }
        else if (action.equals(ON_BUTTON_ACTION)) {
            onButtonAction(callbackContext);
        } else if (action.equals(GET_VALUE)) {
            getValue(callbackContext);
        } else if (action.equals(CLOSE_KEYBOARD)) {
            closeKeyboard(callbackContext);
        } else {
            return false;
        }
        return true;
    }

    private void closeKeyboard(CallbackContext callbackContext) {
        closeKeyboard();
        callbackContext.success();
    }

    private String getValue() {
        String value = mEditText.getText().toString();
        return value;
    }

    private void setup(final CallbackContext callbackContext, final JSONArray args)
            throws JSONException {
        boolean hasRightButton = !args.isNull(RIGHT_BUTTON_ARG);
        boolean hasLeftButton = !args.isNull(LEFT_BUTTON_ARG);

        if (mPanel == null) {
            createBasicUi();
        }

        addEditTextToPanel(hasRightButton && hasLeftButton);

        if (!args.isNull(INPUT_ARG)) {
            setupEditTextOptions(args.getJSONObject(INPUT_ARG));
        }

        if (!args.isNull(PANEL_ARG)) {
            setupPanelOptions(args.getJSONObject(PANEL_ARG));
        }

        removeRightButton();
        if (hasRightButton) {
            addRightButton(args.getJSONObject(RIGHT_BUTTON_ARG), hasLeftButton);
        }

        removeLeftButton();
        if (hasLeftButton) {
            addLeftButton(args.getJSONObject(LEFT_BUTTON_ARG), hasRightButton);
        }

        AGStyler.updateStyle(mEditText);

        callbackContext.success();
    }

    private void show(final CallbackContext callbackContext, final JSONArray args)
            throws JSONException {

        if (!args.isNull(TEXT_ARG)) {
            mEditText.setText(args.optString(TEXT_ARG, ""));
        }

        addPanelBelowWebView();

        callbackContext.success();
    }


    private void setupPanelOptions(JSONObject panelArgs) throws JSONException {
        String styleClass = NATIVE_INPUT_PANEL + " " + panelArgs.optString(STYLE_CLASS, "");
        String styleId = panelArgs.optString(STYLE_ID, "");
        String styleCSS = panelArgs.optString(STYLE_CSS, "");

        AGStyler.setStyleClass(mPanel, styleClass);
        AGStyler.setStyleId(mPanel, styleId);
        AGStyler.setStyle(mPanel, styleCSS);
    }

    private void setupEditTextOptions(JSONObject inputArgs) throws JSONException {
        mEditText.setInputType(getInputType(inputArgs));

        mEditText.setHint(getPlaceholderText(inputArgs));

        int maxLines = getMaxLines(inputArgs);
        mEditText.setMaxLines(maxLines);
        if (maxLines > 1) {
            mEditText.setSingleLine(false);
        } else {
            mEditText.setSingleLine(true);
        }

        String styleClass = NATIVE_INPUT + " " + inputArgs.optString(STYLE_CLASS, "");
        String styleId = inputArgs.optString(STYLE_ID, "");
        String styleCSS = inputArgs.optString(STYLE_CSS, "");

        AGStyler.setStyleClass(mEditText, styleClass);
        AGStyler.setStyleId(mEditText, styleId);
        AGStyler.setStyle(mEditText, styleCSS);
    }

    private String getPlaceholderText(JSONObject inputArgs) {
        return inputArgs.optString(PLACE_HOLDER, "");
    }

    private void addRightButton(JSONObject jsonObject, boolean hasLeftButton) throws JSONException {
        mRightButton = new Button(webView.getContext());


        float weight = hasLeftButton ? 2f : 1f;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                weight);
        params.leftMargin = 0;
        params.rightMargin = 10;
        params.topMargin = 12;
        params.bottomMargin = 10;

        mRightButton.setMinWidth(BUTTON_WIDTH);
        mPanel.addView(mRightButton, params);

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnButtonActionCallback != null) {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, RIGHT);
                    pluginResult.setKeepCallback(true);
                    mOnButtonActionCallback.sendPluginResult(pluginResult);
                }
            }
        });

        String styleClass = NATIVE_INPUT_RIGHT_BUTTON + " " + jsonObject.optString(STYLE_CLASS, "");
        String styleId = jsonObject.optString(STYLE_ID, "");
        String styleCSS = jsonObject.optString(STYLE_CSS, "");

        AGStyler.setStyleClass(mRightButton, styleClass);
        AGStyler.setStyleId(mRightButton, styleId);
        AGStyler.setStyle(mRightButton, styleCSS);
    }

    private void removeRightButton() {
        if (mRightButton != null) {
            mPanel.removeView(mRightButton);
            mRightButton = null;
        }
    }

    private void addLeftButton(JSONObject jsonObject, boolean hasRightButton) {
        mLeftButton = new Button(webView.getContext());

        float weight = hasRightButton ? 2f : 1f;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                weight);
        params.leftMargin = 10;
        params.rightMargin = 0;
        params.topMargin = 12;
        params.bottomMargin = 10;

        mLeftButton.setMinWidth(BUTTON_WIDTH);
        mPanel.addView(mLeftButton, 0, params);

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnButtonActionCallback != null) {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, LEFT);
                    pluginResult.setKeepCallback(true);
                    mOnButtonActionCallback.sendPluginResult(pluginResult);
                }
            }
        });

        String styleClass = NATIVE_INPUT_LEFT_BUTTON + " " + jsonObject.optString(STYLE_CLASS, "");
        String styleId = jsonObject.optString(STYLE_ID, "");
        String styleCSS = jsonObject.optString(STYLE_CSS, "");

        AGStyler.setStyleClass(mLeftButton, styleClass);
        AGStyler.setStyleId(mLeftButton, styleId);
        AGStyler.setStyle(mLeftButton, styleCSS);
    }

    private void removeLeftButton() {
        if (mLeftButton != null) {
            mPanel.removeView(mLeftButton);
            mLeftButton = null;
        }
    }

    private void addPanelBelowWebView() {
        ViewGroup parentView = (ViewGroup) webView.getParent();
        parentView.removeView(mPanel);
        parentView.addView(mPanel);
    }

    private void removeEditTextFromBelowWebView() {
        ViewGroup parentView = (ViewGroup) webView.getParent();
        parentView.removeView(mPanel);
    }

    private int getMaxLines(JSONObject inputArgs) throws JSONException {
        int maxLines = 1;
        if (!inputArgs.isNull(LINES)) {
            maxLines = inputArgs.getInt(LINES);
        }
        return maxLines;
    }

    private boolean find(String text, JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (text.equalsIgnoreCase(array.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private int getInputType(JSONObject inputArgs) throws JSONException {
        int inputType = EditorInfo.TYPE_CLASS_TEXT;

        if (!inputArgs.isNull(TYPE)) {
            String type = inputArgs.getString(TYPE);

            if (URI.equalsIgnoreCase(type)) {
                inputType = EditorInfo.TYPE_TEXT_VARIATION_URI;
            }
            if (EMAIL.equalsIgnoreCase(type)) {
                inputType = EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            }
            if (NUMBER.equalsIgnoreCase(type)) {
                inputType = EditorInfo.TYPE_NUMBER_VARIATION_NORMAL;
            }
        }

        return inputType;
    }


    private void hide(final CallbackContext callbackContext) {
        if (mEditText != null) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeEditTextFromBelowWebView();
                    closeKeyboard();
                    callbackContext.success();
                }
            });
        } else {
            callbackContext.error("No Native Input available.");
        }
    }

    private void onButtonAction(CallbackContext callbackContext) {
        mOnButtonActionCallback = callbackContext;
    }

    private void onKeyboardClose(CallbackContext callbackContext, JSONArray args) {
        mOnKeyboardCloseCallback = callbackContext;
    }

    private void onKeyboardAction(CallbackContext callbackContext, JSONArray args) {
        mAutoCloseKeyboard = args.optBoolean(AUTO_CLOSE_KEYBOARD, true);
        mOnKeyboardActionCallback = callbackContext;
    }

    private void onChange(CallbackContext callbackContext) {
        mOnChangeCallback = callbackContext;
    }

    private void getValue(CallbackContext callbackContext) {
        String value = getValue();
        callbackContext.success(value);
    }

    private String getActionName(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            return DONE;
        } else if (actionId == EditorInfo.IME_ACTION_GO) {
            return GO;
        } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
            return NEXT;
        } else if (actionId == EditorInfo.IME_ACTION_SEND) {
            return SEND;
        } else if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            return NEWLINE;
        }
        return null;
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) cordova.getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void setupPixate() {
        if (!sPixateInitiated) {
            sPixateInitiated = true;
            AGStyler.loadStyler("com.appgyver.plugins.nativeinput.CustomEditText",
                    "com.appgyver.plugins.nativeinput.CustomEditTextAdapter");
        }
    }
}
