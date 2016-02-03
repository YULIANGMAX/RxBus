package cnnj.yuliangmax.rxbussample;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class RxBus {

//    private static final String TAG = RxBus.class.getSimpleName();

    private Executor executor;
    private Handler handler;

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    //////////////////////////////////////////////////////////////
    private RxBus() {
    }

    private static class InstanceHolder {
        public static RxBus instance = new RxBus();
    }

    public static RxBus getInstance() {
        return InstanceHolder.instance;
    }
    //////////////////////////////////////////////////////////////

    private ConcurrentHashMap<String, List<Subject>> subjectMapper = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Object, ConcurrentHashMap<Method, Subject>> subscriberMapper = new ConcurrentHashMap<>();

    public void register(@NonNull final Object subscriber) {
        if (subscriberMapper.containsKey(subscriber)) {
            return;
        }
        Class<?> clazz = subscriber.getClass();
        while (clazz != null) {
            final Method[] allMethods = clazz.getDeclaredMethods();
            for (int i = 0; i < allMethods.length; i++) {
                final Method method = allMethods[i];
                SubscriberAnnotation annotation = method.getAnnotation(SubscriberAnnotation.class);
                if (annotation != null) {
                    Class<?>[] paramsTypeClass = method.getParameterTypes();
                    if (paramsTypeClass != null && paramsTypeClass.length == 1) {// 订阅函数只支持一个参数
                        List<Subject> subjectList = subjectMapper.get(annotation.tag());
                        if (null == subjectList) {
                            subjectList = new ArrayList<>();
                            subjectMapper.put(annotation.tag(), subjectList);
                        }

                        ConcurrentHashMap<Method, Subject> msMap = subscriberMapper.get(subscriber);
                        if (null == msMap) {
                            msMap = new ConcurrentHashMap<>();
                            subscriberMapper.put(subscriber, msMap);
                        }

                        Subject subject = PublishSubject.create();
                        subjectList.add(subject);
                        msMap.put(method, subject);

                        Observable observable;
                        switch (annotation.scheduler()) {
                            case NEW_THREAD:
                                observable = subject.observeOn(Schedulers.newThread());
                                break;
                            case IO:
                                observable = subject.observeOn(Schedulers.io());
                                break;
                            case IMMEDIATE:
                                observable = subject.observeOn(Schedulers.immediate());
                                break;
                            case COMPUTATION:
                                observable = subject.observeOn(Schedulers.computation());
                                break;
                            case TRAMPOLINE:
                                observable = subject.observeOn(Schedulers.trampoline());
                                break;
                            case EXECUTOR:
                                if (null == executor) {
                                    throw new RuntimeException("executor is null, please setExecutor in Application");
                                }
                                observable = subject.observeOn(Schedulers.from(executor));
                                break;
                            case HANDLER:
                                if (null == handler) {
                                    throw new RuntimeException("handler is null, please setHandler in Application");
                                }
                                observable = subject.observeOn(HandlerScheduler.from(handler));
                                break;
                            default:
                                observable = subject.observeOn(AndroidSchedulers.mainThread());
                                break;
                        }
                        observable.subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                try {
                                    method.invoke(subscriber, o);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public void unregister(@NonNull Object subscriber) {
        if (!subscriberMapper.containsKey(subscriber)) {
            return;
        }
        Class<?> clazz = subscriber.getClass();
        while (clazz != null) {
            final Method[] allMethods = clazz.getDeclaredMethods();
            for (int i = 0; i < allMethods.length; i++) {
                final Method method = allMethods[i];
                SubscriberAnnotation annotation = method.getAnnotation(SubscriberAnnotation.class);
                if (annotation != null) {
                    List<Subject> subjects = subjectMapper.get(annotation.tag());
                    if (null != subjects) {
                        ConcurrentHashMap<Method, Subject> msMap = subscriberMapper.get(subscriber);
                        subjects.remove(msMap.remove(method));
                        if (subjects == null || subjects.isEmpty()) {
                            subjectMapper.remove(annotation.tag());
                        }
                        if (msMap == null || msMap.isEmpty()) {
                            subscriberMapper.remove(subscriber);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public void post(@NonNull Object event, @NonNull Object tag) {
        List<Subject> subjectList = subjectMapper.get(tag);
        if (subjectList != null && !subjectList.isEmpty()) {
            for (Subject subject : subjectList) {
                subject.onNext(event);
            }
        }
    }

}