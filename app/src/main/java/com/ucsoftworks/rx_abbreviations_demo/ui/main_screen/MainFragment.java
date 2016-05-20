package com.ucsoftworks.rx_abbreviations_demo.ui.main_screen;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.ucsoftworks.rx_abbreviations_demo.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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

        subscription = RxTextView
                .textChanges(abbreviationEditText)
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnEach(s -> setAdapter(new ArrayList<>())) //при изменении текста очищаем список результатов
                .debounce(TIMEOUT, TimeUnit.SECONDS) //если пользователь ничего не делал 1 секунду, то обрабатываем результат ввода. В противном случае игнорируем.
                .filter(s -> !TextUtils.isEmpty(s) && s.length() > 1) //отсекаем слишком короткие строчки
                .observeOn(AndroidSchedulers.mainThread()) //следующее действие обратываем в главном потоке
                .doOnEach(s -> setProgressIndicator(View.VISIBLE, View.GONE)) //перед запросом к сети показываем индикатор прогресса
                .flatMap(s -> abbreviationsApi
                        .getAbbreviationsResponse(s)
                        .subscribeOn(Schedulers.io()) //работу с сетью выполняем в отдельном потоке
                        .observeOn(AndroidSchedulers.mainThread()) //результат получаем в главном потоке
                        .doOnError(MainFragment.this::handleError) //вызов метода в случае какой-нибудь ошибки
                        .onExceptionResumeNext(Observable.empty()) //при ошибке не прерываем цепочку, просто ничего не делаем
                )
                .doOnEach(s -> setProgressIndicator(View.GONE, View.VISIBLE)) //после запроса к сети показываем индикатор прогресса
                .subscribeOn(Schedulers.io()) //промежуточные вычисления и преобразования выполняем в отдельном потоке
                .filter(searchResponses -> searchResponses.size() == 1)
                .map(searchResponses -> searchResponses.get(0).getLfs())
                .map(lfs -> Observable //трансформируем массив типа Lf в массив строк
                        .from(lfs)
                        .map(Lf::getLf)
                        .toList()
                        .toBlocking()
                        .first()
                )
                    /*Классический подход:
                    final List<Lf> lfs = searchResponses.get(0).getLfs();
                    ArrayList<String> strings = new ArrayList<>(lfs.size());
                    for (Lf lf : lfs)
                        strings.add(lf.getLf());
                    return strings;
                    */
                .observeOn(AndroidSchedulers.mainThread()) //результат получаем в главном потоке
                .subscribe(
                        this::setAdapter, //сюда придут уже преобразованные данные
                        MainFragment.this::handleError, //вызовется в случае какой-нибудь ошибки
                        () -> Log.d("Rx view", "onCompleted") //не вызовется, так как в самом первом Observable не вызывается onComplete
                );

    }

    private void setAdapter(List<String> strings) {
        if (isVisible())
            listView.setAdapter(getStringArrayAdapter(strings));
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


    private void handleError(Throwable e) {
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
