package org.seo.rank;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.tagging.SynonymTagging;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoukai on 2016/10/19.
 */
public class WordTest {
    public static void main(String[] args) {
        //分词
        List<Word> words1 = WordSegmenter.seg("杨尚川是APDPlat应用级产品开发平台的作者 zhoukai  li bai", SegmentationAlgorithm.MaxNgramScore);
        List<Word> words2 = WordSegmenter.seg("杨尚川是APDPlat应用级产品开发平台的作者", SegmentationAlgorithm.MaxNgramScore);
//        SynonymTagging.process(words1);
        System.out.println("word1" + words1);
        System.out.println("word2" + words2);
        //词频统计
        Map<Word, AtomicInteger> blog1WordsFre = frequence(words1);
        Map<Word, AtomicInteger> blog2WordsFre = frequence(words2);
        //使用简单共有词判定
        double score = simpleScore(blog1WordsFre, blog2WordsFre);
        System.out.println(score);
        //使用余弦相似度判定
        score = cosScore(blog1WordsFre, blog2WordsFre);
        System.out.println(score);
    }

    private static Map<Word, AtomicInteger> frequence(List<Word> words) {
        Map<Word, AtomicInteger> fre = new HashMap<>();
        words.forEach(word -> {
            fre.putIfAbsent(word, new AtomicInteger());
            fre.get(word).incrementAndGet();
        });
        return fre;
    }

    /**
     * 判定相似性的方式一：简单共有词
     *
     * @param blog1WordsFre
     * @param blog2WordsFre
     * @return
     */
    private static double simpleScore(Map<Word, AtomicInteger> blog1WordsFre, Map<Word, AtomicInteger> blog2WordsFre) {
        //判断有几个相同的词
        AtomicInteger intersectionLength = new AtomicInteger();
        blog1WordsFre.keySet().forEach(word -> {
            if (blog2WordsFre.keySet().contains(word)) {
                intersectionLength.incrementAndGet();
            }
        });
        System.out.println("网页1有的词数：" + blog1WordsFre.size());
        System.out.println("网页2有的词数：" + blog2WordsFre.size());
        System.out.println("网页1和2共有的词数：" + intersectionLength.get());
        double score = intersectionLength.get() / (double) Math.min(blog1WordsFre.size(), blog2WordsFre.size());
        System.out.println("相似度分值=" + intersectionLength.get() + "/(double)Math.min(" + blog1WordsFre.size() + ", " + blog2WordsFre.size() + ")=" + score);
        return score;
    }

    /**
     * 判定相似性的方式二：余弦相似度
     * 余弦夹角原理：
     * 向量a=(x1,y1),向量b=(x2,y2)
     * a.b=x1x2+y1y2
     * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
     * a,b的夹角的余弦cos=a.b/|a|*|b|=(x1x2+y1y2)/根号[(x1)^2+(y1)^2]*根号[(x2)^2+(y2)^2]
     *
     * @param blog1WordsFre
     * @param blog2WordsFre
     */
    private static double cosScore(Map<Word, AtomicInteger> blog1WordsFre, Map<Word, AtomicInteger> blog2WordsFre) {
        Set<Word> words = new HashSet<>();
        words.addAll(blog1WordsFre.keySet());
        words.addAll(blog2WordsFre.keySet());
        //向量的维度为words的大小，每一个维度的权重是词频，注意的是，中文分词的时候已经去了停用词
        //a.b
        AtomicInteger ab = new AtomicInteger();
        //|a|
        AtomicInteger aa = new AtomicInteger();
        //|b|
        AtomicInteger bb = new AtomicInteger();
        //计算
        words
                .stream()
                .forEach(word -> {
                    AtomicInteger x1 = blog1WordsFre.get(word);
                    AtomicInteger x2 = blog2WordsFre.get(word);
                    if (x1 != null && x2 != null) {
                        //x1x2
                        int oneOfTheDimension = x1.get() * x2.get();
                        //+
                        ab.addAndGet(oneOfTheDimension);
                    }
                    if (x1 != null) {
                        //(x1)^2
                        int oneOfTheDimension = x1.get() * x1.get();
                        //+
                        aa.addAndGet(oneOfTheDimension);
                    }
                    if (x2 != null) {
                        //(x2)^2
                        int oneOfTheDimension = x2.get() * x2.get();
                        //+
                        bb.addAndGet(oneOfTheDimension);
                    }
                });

        double aaa = Math.sqrt(aa.get());
        double bbb = Math.sqrt(bb.get());
        //使用BigDecimal保证精确计算浮点数
        BigDecimal aabb = BigDecimal.valueOf(aaa).multiply(BigDecimal.valueOf(bbb));
        double cos = ab.get() / aabb.doubleValue();
        return cos;
    }
}
