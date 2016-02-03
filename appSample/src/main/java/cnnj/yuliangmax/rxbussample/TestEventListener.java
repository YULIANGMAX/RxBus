package cnnj.yuliangmax.rxbussample;

public abstract class TestEventListener {

    @SubscriberAnnotation(tag = EventTags.TAG_AAA, scheduler = ThreadScheduler.NEW_THREAD)
    public abstract void onLisTestEventAAA(TestEvent event);

    @SubscriberAnnotation(tag = EventTags.TAG_BBB)
    public abstract void onLisTestEventBBB(TestEvent event);

}
