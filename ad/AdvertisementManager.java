package com.javarush.task.task27.task2712.ad;

import com.javarush.task.task27.task2712.ConsoleHelper;
import com.javarush.task.task27.task2712.statistic.StatisticManager;
import com.javarush.task.task27.task2712.statistic.event.VideoSelectedEventDataRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdvertisementManager {
    private final AdvertisementStorage storage = AdvertisementStorage.getInstance();
    private int timeSeconds;

    public AdvertisementManager(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public void processVideos() {
//        ConsoleHelper.writeMessage("calling processVideos method");
        if (storage.list().isEmpty()) throw new NoVideoAvailableException();
        printAd(maxBenefitList(new ArrayList<Advertisement>(), new ArrayList<Advertisement>(), timeSeconds, 0));
    }

    private void printAd(List<Advertisement> advertisements) {
        if (!advertisements.isEmpty()) StatisticManager.getInstance().register(new VideoSelectedEventDataRow(
                advertisements, advertismentsCost(advertisements), advertismentsTime(advertisements)));
        advertisements.sort(new Comparator<Advertisement>() {
            @Override
            public int compare(Advertisement o1, Advertisement o2) {
                int c1 = (int) (o2.getAmountPerOneDisplaying() - o1.getAmountPerOneDisplaying());
                if (c1 != 0) return c1;
                int c2 = (int) (o1.getAmountPerOneDisplaying() * 1000 / o1.getDuration() -
                                o2.getAmountPerOneDisplaying() * 1000 / o2.getDuration());
                return c2;
            }
        });
        for (Advertisement a : advertisements) {
            ConsoleHelper.writeMessage(a.toString());
            a.revalidate();
        }
    }

    private List<Advertisement> maxBenefitList(List<Advertisement> candidate, List<Advertisement> leaderList, int maxTime, int curTime) {
        if (candidate.size() < storage.list().size()) {
            List<Advertisement> rest = new ArrayList<>(storage.list()); Collections.reverse(rest);
            rest.removeAll(candidate);
            if (curTime < maxTime) { // ???????????????? ?????? ?????????? ?????????? ???????????? ???????????? maxTime, ??.??. ????????????????
                for (Advertisement a : rest) {
                    if (a.getHits() > 0 && (a.getDuration() + curTime <= maxTime)) {
                        List<Advertisement> n = new ArrayList<>(candidate);
                        n.add(a);
                        leaderList = maxBenefitList(n, leaderList, maxTime, a.getDuration() + curTime);
                    }
                }
            }
        }
        leaderList = choose(leaderList, candidate, maxTime);
        return leaderList;
    }

    private List<Advertisement> choose(List<Advertisement> first, List<Advertisement> second, int maxTime) {
        int firstTime = advertismentsTime(first);
        int secondTime = advertismentsTime(second);
        if (firstTime > maxTime && secondTime <= maxTime) return second;
        // ???????? ???????????? ?????????? ???????????? maxTime, ?? ???????????? ??????, ???? ???????????? ????????????????????
        if (secondTime > maxTime && firstTime <= maxTime) return first;
        // ???????? ???????????? ?????????? ???????????? maxTime, ?? ???????????? ??????, ???? ???????????? ????????????????????
        // ?????????? ?????????? ???????????????? ???? ??????????????????
        long firstCost = advertismentsCost(first);
        long secondCost = advertismentsCost(second);
        if (firstCost > secondCost) return first;
        if (firstCost < secondCost) return second;
        // ?????????? ???????? ?????????????????? ????????????????????
        if (firstTime > secondTime) return first;
        if (firstTime < secondTime) return second;
        // ?????????? ???????? ?????????? ???????????????????? ???? ???????????? ?????????? ?? ?????????????????????? ?????????????????????? ??????????????
        int firstCounts = first.size();
        int secondCounts = second.size();
        return firstCounts < secondCounts ? first : second;
    }

    private int advertismentsTime(List<Advertisement> advertisements) {
        int result = 0;
        for (Advertisement advertisement : advertisements) {
            result += advertisement.getDuration();
        }
        return result;
    }


    private long advertismentsCost(List<Advertisement> advertisements) {
        long result = 0;
        for (Advertisement advertisement : advertisements) {
            result += advertisement.getAmountPerOneDisplaying();
        }
        return result;
    }



}


