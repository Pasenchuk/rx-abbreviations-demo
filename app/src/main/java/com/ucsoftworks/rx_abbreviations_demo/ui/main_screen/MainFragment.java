package com.ucsoftworks.rx_abbreviations_demo.ui.main_screen;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.ucsoftworks.rx_abbreviations_demo.R;
import com.ucsoftworks.rx_abbreviations_demo.app.App;
import com.ucsoftworks.rx_abbreviations_demo.network.AbbreviationsApi;
import com.ucsoftworks.rx_abbreviations_demo.network.models.Lf;
import com.ucsoftworks.rx_abbreviations_demo.network.models.SearchResponse;
import com.ucsoftworks.rx_abbreviations_demo.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends BaseFragment {

    public static final int TIMEOUT = 1;
    @Inject
    AbbreviationsApi abbreviationsApi;
    @Bind(R.id.abbreviation)
    EditText abbreviationEditText;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.list_view)
    ListView listView;
    private Observable<String> stringObservable;
    private Subscription subscription;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getApp(this).getAppComponent().inject(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        subscription = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                abbreviationEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        subscriber.onNext(charSequence.toString());

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
        })
                .debounce(TIMEOUT, TimeUnit.SECONDS) //если пользователь ничего не делал 1 секунду, то обрабатываем результат ввода. В противном случае игнорируем.
                .observeOn(AndroidSchedulers.mainThread()) //следующий коллбэк переводим в UI поток (будем изменять Views)
                .filter(s -> { //отсекаем слишком короткие строчки
                    final boolean b = !TextUtils.isEmpty(s) && s.length() > 1;
                    if (b) {
                        setProgressIndicator(View.VISIBLE, View.GONE);
                    }
                    return b;
                })
                .observeOn(Schedulers.io())//следущее действие отслеживаем в отдельном потоке
                .flatMap(s -> abbreviationsApi.getObservableResponse(s))
                .doOnError(MainFragment.this::onError /*вызовется в случае какой-нибудь ошибки */)
                .retry()
                .map((Func1<List<SearchResponse>, List<String>>) searchResponses -> {
                    Log.d("Rx view", "flatMap List<String>");
                    if (searchResponses.size() != 1)
                        return new ArrayList<>();
                    final List<Lf> lfs = searchResponses.get(0).getLfs();
                    ArrayList<String> strings = new ArrayList<>(lfs.size());
                    for (Lf lf : lfs)
                        strings.add(lf.getLf());
                    return strings;
                })
                .subscribeOn(Schedulers.io()) //работу с сетью и преобразования выполняем в отдельном потоке
                .observeOn(AndroidSchedulers.mainThread()) //результат получаем в главном потоке
                .subscribe(
                        strings -> {
                            //сюда придут уже преобразованные данные
                            setProgressIndicator(View.GONE, View.VISIBLE);
                            Log.d("Rx view", "onNext");
                            if (isVisible())
                                listView.setAdapter(getStringArrayAdapter(strings));
                        },
                        MainFragment.this::onError /*вызовется в случае какой-нибудь ошибки */,
                        () -> {
                            Log.d("Rx view", "onCompleted");//не вызовется, так как в самом первом Observable не вызывается onComplete
                        }
                );

    }

    @Override
    public void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void onError(Throwable e) {
        setProgressIndicator(View.GONE, View.VISIBLE);
        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }

    private void setProgressIndicator(int progress, int list) {
        if (isVisible()) {
            progressBar.setVisibility(progress);
            listView.setVisibility(list);
        }
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter(List<String> strings) {
        return new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, strings);
    }
}
