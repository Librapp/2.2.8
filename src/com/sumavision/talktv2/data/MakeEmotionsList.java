package com.sumavision.talktv2.data;

import java.util.ArrayList;
import java.util.List;

import com.sumavision.talktv2.R;

/**
 * 
 * @author 郭鹏
 * 
 */
public class MakeEmotionsList {

	private static MakeEmotionsList instance;
	private List<EmotionData> le;
	private final int EMOTIONCOUNT = 45;

	public List<EmotionData> getLe() {
		le = new ArrayList<EmotionData>();

		for (int i = 0; i < EMOTIONCOUNT; i++) {
			EmotionData e = new EmotionData();

			switch (i) {
			case 0:
				e.setPhrase("[哈哈]");
				e.setOrderNumber(0);
				e.setImageName("hh");
				e.setId(R.drawable.hh);

				break;
			case 1:
				e.setPhrase("[呵呵]");
				e.setOrderNumber(1);
				e.setImageName("hehe");
				e.setId(R.drawable.hehe);

				break;
			case 2:
				e.setPhrase("[酷]");
				e.setOrderNumber(2);
				e.setImageName("cool");
				e.setId(R.drawable.cool);

				break;
			case 3:
				e.setPhrase("[花心]");
				// e.setPhraseOther("[心]");
				e.setOrderNumber(3);
				e.setImageName("huaxin");
				e.setId(R.drawable.huaxin);

				break;
			case 4:
				e.setPhrase("[可爱]");
				e.setOrderNumber(4);
				e.setImageName("keai");
				e.setId(R.drawable.keai);

				break;
			case 5:
				e.setPhrase("[偷笑]");
				e.setOrderNumber(5);
				e.setImageName("tx");
				e.setId(R.drawable.tx);

				break;
			case 6:
				e.setPhrase("[调皮]");
				e.setOrderNumber(6);
				e.setImageName("tp");
				e.setId(R.drawable.tp);

				break;
			case 7:
				e.setPhrase("[害羞]");
				e.setOrderNumber(7);
				e.setImageName("hx");
				e.setId(R.drawable.hx);

				break;
			case 8:
				e.setPhrase("[泪]");
				e.setOrderNumber(8);
				e.setImageName("lei");
				e.setId(R.drawable.lei);

				break;
			case 9:
				e.setPhrase("[大哭]");
				e.setOrderNumber(9);
				e.setImageName("dk");
				e.setId(R.drawable.dk);

				break;
			case 10:
				e.setPhrase("[鄙视]");
				e.setOrderNumber(10);
				e.setImageName("bs");
				e.setId(R.drawable.bs);

				break;
			case 11:
				e.setPhrase("[白眼]");
				e.setOrderNumber(11);
				e.setImageName("by");
				e.setId(R.drawable.by);

				break;
			case 12:
				e.setPhrase("[闭嘴]");
				e.setOrderNumber(12);
				e.setImageName("bz");
				e.setId(R.drawable.bz);

				break;
			case 13:
				e.setPhrase("[吃惊]");
				e.setOrderNumber(13);
				e.setImageName("cj");
				e.setId(R.drawable.cj);

				break;
			case 14:
				e.setPhrase("[大哭]");
				e.setOrderNumber(14);
				e.setImageName("dk");
				e.setId(R.drawable.dk);

				break;
			case 15:
				e.setPhrase("[愤怒]");
				e.setOrderNumber(15);
				e.setImageName("fn");
				e.setId(R.drawable.fn);

				break;
			case 16:
				e.setPhrase("[尴尬]");
				e.setOrderNumber(16);
				e.setImageName("gg");
				e.setId(R.drawable.gg);

				break;
			case 17:
				e.setPhrase("[good]");
				e.setOrderNumber(17);
				e.setImageName("good");
				e.setId(R.drawable.good);

				break;
			case 18:
				e.setPhrase("[汗]");
				e.setOrderNumber(18);
				e.setImageName("han");
				e.setId(R.drawable.han);

				break;
			case 19:
				e.setPhrase("[握手]");
				e.setOrderNumber(19);
				e.setImageName("hand");
				e.setId(R.drawable.hand);

				break;
			case 20:
				e.setPhrase("[惊恐]");
				e.setOrderNumber(20);
				e.setImageName("jk");
				e.setId(R.drawable.jk);

				break;
			case 21:
				e.setPhrase("[热吻]");
				// e.setPhraseOther("[花]");
				e.setOrderNumber(21);
				e.setImageName("kiss");
				e.setId(R.drawable.kiss);

				break;
			case 22:
				e.setPhrase("[困]");
				e.setOrderNumber(22);
				e.setImageName("kun");
				e.setId(R.drawable.kun);

				break;
			case 23:
				e.setPhrase("[钱]");
				e.setOrderNumber(23);
				e.setImageName("money");
				e.setId(R.drawable.money);

				break;
			case 24:
				e.setPhrase("[月亮]");
				e.setOrderNumber(24);
				e.setImageName("moon");
				e.setId(R.drawable.moon);

				break;
			case 25:
				e.setPhrase("[ok]");
				e.setOrderNumber(25);
				e.setImageName("ok");
				e.setId(R.drawable.ok);

				break;
			case 26:
				e.setPhrase("[撇嘴]");
				e.setOrderNumber(26);
				e.setImageName("pz");
				e.setId(R.drawable.pz);

				break;
			case 27:
				e.setPhrase("[弱]");
				e.setOrderNumber(27);
				e.setImageName("ruo");
				e.setId(R.drawable.ruo);

				break;
			case 28:
				e.setPhrase("[衰]");
				// e.setPhraseOther("[伤心]");
				e.setOrderNumber(28);
				e.setImageName("shuai");
				e.setId(R.drawable.shuaiping);

				break;
			case 29:
				e.setPhrase("[睡觉]");
				e.setOrderNumber(29);
				e.setImageName("sj");
				e.setId(R.drawable.sj);

				break;
			case 30:
				e.setPhrase("[思考]");
				e.setOrderNumber(30);
				e.setImageName("sk");
				e.setId(R.drawable.sk);

				break;
			case 31:
				e.setPhrase("[失望]");
				e.setOrderNumber(31);
				e.setImageName("sw");
				e.setId(R.drawable.sw);

				break;
			case 32:
				e.setPhrase("[吐]");
				// e.setPhraseOther("[便便]");
				e.setOrderNumber(32);
				e.setImageName("tu");
				e.setId(R.drawable.tu);

				break;
			case 33:
				e.setPhrase("[委屈]");
				e.setOrderNumber(33);
				e.setImageName("wq");
				e.setId(R.drawable.wq);

				break;
			case 34:
				e.setPhrase("[嘘]");
				e.setOrderNumber(34);
				e.setImageName("xu");
				e.setId(R.drawable.xu);

				break;
			case 35:
				e.setPhrase("[耶]");
				e.setOrderNumber(35);
				e.setImageName("yeah");
				e.setId(R.drawable.yeah);

				break;
			case 36:
				e.setPhrase("[晕]");
				e.setOrderNumber(36);
				e.setImageName("yun");
				e.setId(R.drawable.yun);

				break;
			case 37:
				e.setPhrase("[疑问]");
				e.setOrderNumber(37);
				e.setImageName("yw");
				e.setId(R.drawable.yw);

				break;
			case 38:
				e.setPhrase("[做鬼脸]");
				e.setOrderNumber(38);
				e.setImageName("zgl");
				e.setId(R.drawable.zgl);

				break;
			case 39:
				e.setPhrase("[拜拜]");
				e.setOrderNumber(39);
				e.setImageName("zj");
				e.setId(R.drawable.zj);

				break;
			case 40:
				e.setPhrase("[抓狂]");
				e.setOrderNumber(40);
				e.setImageName("zk");
				e.setId(R.drawable.zk);

				break;
			case 41:
				e.setPhrase("[猪头]");
				e.setOrderNumber(41);
				e.setImageName("zt");
				e.setId(R.drawable.zt);

				break;
			case 42:
				e.setPhrase("[花]");
				e.setOrderNumber(42);
				e.setImageName("hua");
				e.setId(R.drawable.hua);

				break;
			case 43:
				e.setPhrase("[伤心]");
				e.setOrderNumber(43);
				e.setImageName("sad");
				e.setId(R.drawable.sad);

				break;
			case 44:
				e.setPhrase("[便便]");
				e.setOrderNumber(44);
				e.setImageName("shit");
				e.setId(R.drawable.shit);

				break;
			case 45:
				e.setPhrase("[心]");
				e.setOrderNumber(45);
				e.setImageName("xin");
				e.setId(R.drawable.xin);
				break;
			default:
				break;
			}

			le.add(e);
		}

		return le;
	}

	public void setLe(List<EmotionData> le) {
		this.le = le;
	}

	public static MakeEmotionsList current() {
		if (instance == null) {
			instance = new MakeEmotionsList();
		}
		return instance;
	}
}
