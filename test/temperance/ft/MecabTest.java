package temperance.ft;

import java.util.List;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.Assert;
import org.junit.Test;

import temperance.hash.Digest;
import temperance.hashing.MecabHashing;

public class MecabTest {

    @Test
    public void parseStr_default() {
        MecabHashing mecab = new MecabHashing(Digest.MD5, Tagger.create("-r /opt/local/etc/mecabrc"));
        {
            List<String> surfaces = mecab.parseToString("本日は");
            Assert.assertEquals(surfaces.size(), 2); // "本日" "は"
        }
        {
            List<String> surfaces = mecab.parseToString("本日は【晴天】です");
            Assert.assertEquals(surfaces.size(), 6); // "本日" "は" "【" "晴天" "】" "です"
        }
        {
            List<String> surfaces = mecab.parseToString("本日は　晴天　です");
            Assert.assertEquals(surfaces.size(), 6); // "本日" "は" "【" "晴天" "】" "です"
        }
    }

    @Test
    public void parseStr_nouns() {
        MecabHashing mecab = new MecabHashing(Digest.MD5, Tagger.create("-r /opt/local/etc/mecabrc"), MecabHashing.Filter.Nouns);
        {
            List<String> surfaces = mecab.parseToString("本日は");
            Assert.assertEquals(surfaces.size(), 1); // "本日"
        }
        {
            List<String> surfaces = mecab.parseToString("本日は【晴天】です");
            Assert.assertEquals(surfaces.size(), 2); // "本日" "晴天"
        }
        {
            List<String> surfaces = mecab.parseToString("本日は　晴天　です");
            Assert.assertEquals(surfaces.size(), 2); // "本日" "晴天"
        }
    }
    
    @Test
    public void parseStr_nouns_numeric() {
        MecabHashing mecab = new MecabHashing(Digest.MD5, Tagger.create("-r /opt/local/etc/mecabrc"), MecabHashing.Filter.Nouns);
        {
            List<String> surfaces = mecab.parseToString("2010年度");
            Assert.assertEquals(surfaces.size(), 2);
            Assert.assertTrue(surfaces.contains("2010"));
            Assert.assertTrue(surfaces.contains("年度"));
        }
    }
    
}
