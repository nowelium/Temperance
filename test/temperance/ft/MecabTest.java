package temperance.ft;

import java.util.List;

import org.chasen.mecab.wrapper.Tagger;
import org.junit.Assert;
import org.junit.Test;

import temperance.hash.Hash;


public class MecabTest {

    @Test
    public void parseStr_default() {
        Mecab mecab = new Mecab(Hash.MD5, Tagger.create("-r /opt/local/etc/mecabrc"));
        {
            List<Long> hashes = mecab.parse("本日は");
            Assert.assertEquals(hashes.size(), 2); // "本日" "は"
        }
        {
            List<Long> hashes = mecab.parse("本日は【晴天】です");
            Assert.assertEquals(hashes.size(), 6); // "本日" "は" "【" "晴天" "】" "です"
        }
        {
            List<Long> hashes = mecab.parse("本日は　晴天　です");
            Assert.assertEquals(hashes.size(), 6); // "本日" "は" "【" "晴天" "】" "です"
        }
    }

    @Test
    public void parseStr_nouns() {
        Mecab mecab = new Mecab(Hash.MD5, Tagger.create("-r /opt/local/etc/mecabrc"), Mecab.Filter.Nouns);
        {
            List<Long> hashes = mecab.parse("本日は");
            Assert.assertEquals(hashes.size(), 1); // "本日"
        }
        {
            List<Long> hashes = mecab.parse("本日は【晴天】です");
            Assert.assertEquals(hashes.size(), 2); // "本日" "晴天"
        }
        {
            List<Long> hashes = mecab.parse("本日は　晴天　です");
            Assert.assertEquals(hashes.size(), 2); // "本日" "晴天"
        }
    }
}
