
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static String oldWord = "しりとり";
    public static String kanji = "";
    public static String imi = "";
    public static String furigana = "";
    public static boolean test = false;

    public static void main(String[] args) throws MalformedURLException {
        game();

    }

    public static void game() throws MalformedURLException {
        ArrayList<String> lists = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String newWord;
        boolean IsPlayer1turn = true;
        System.out.println("\u001B[32m*************ゲームスタート************\u001B[0m");
        System.out.println("\u001B[31m*************  ルール    ***********\u001B[0m");
        System.out.println("\u001B[31m* １．二文字以上書いてください！  　　　　*\u001B[0m");
        System.out.println("\u001B[31m* ２．同じ言葉を書かないでください！  　　*\u001B[0m");
        System.out.println("\u001B[31m* ３．んで終わる言葉を書いた人は負ける！ 　*\u001B[0m");
        System.out.println("\u001B[32m*　４．カタカナで打ってもかまわない　　　　*\u001B[0m");
        System.out.println("\u001B[32m*　5．ー、ゃ、ゅ、ょで終わる言葉は前の文字になります。*\u001B[0m");
        System.out.println("\u001B[31m*************  ルール    ***********\u001B[0m");
        while (true) {
            String playerJunBan = IsPlayer1turn ? "\u001B[34;1mプレーヤー１の順です。\u001B[0m" : "\u001B[34;1mプレーヤー２の順です。\u001B[0m";
            char oldWordLastCharacter = oldWord.charAt(oldWord.length() - 1);
            if (oldWordLastCharacter == 'ー' || oldWordLastCharacter == 12419 || oldWordLastCharacter == 12421 || oldWordLastCharacter == 12423) {
                oldWordLastCharacter = oldWord.charAt(oldWord.length() - 2);
            }
            System.out.println(playerJunBan);
            System.out.println("'" + oldWordLastCharacter + "'" + " \u001B[32mで始まる言葉を書いて！\u001B[0m");
            newWord = scanner.nextLine();
            if (Character.UnicodeBlock.of(newWord.charAt(0)).equals(Character.UnicodeBlock.BASIC_LATIN)) {
                System.out.println("ひらがな文字にへんこうしています。");
                if (RomajiToHiraganaConverter(newWord) != "error") {
                    newWord = RomajiToHiraganaConverter(newWord);
                }
            }
            if (Character.UnicodeBlock.of(newWord.charAt(0)).equals(Character.UnicodeBlock.KATAKANA)) {
                newWord = convertKatakanatoHiragana(newWord);
                System.out.println("ひらがな文字にへんこうしています。");
            }
            if(Character.UnicodeBlock.of(newWord.charAt(0)).equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)){
//                ArrayList<String> jishoSearchReturn=JishoSearch(URLEncoder.encode(newWord));
                newWord=furigana;
//                System.out.println(newWord);
            }
            ArrayList<String> jishoSearchReturn = JishoSearch(URLEncoder.encode(newWord));
            if (jishoSearchReturn.get(0) != "error") {
                kanji = jishoSearchReturn.get(1).equals("error") ? newWord : jishoSearchReturn.get(1);
                imi = jishoSearchReturn.get(0);
                furigana = jishoSearchReturn.get(2);
//                oldWord=furigana;
                test = true;
            } else {
//                kanji=newWord;
                test = false;
            }

            char newWordFirstCharacter = newWord.charAt(0);
            if (Validation(oldWordLastCharacter, newWordFirstCharacter, lists, newWord)) {
                IsPlayer1turn = !IsPlayer1turn;
                lists.add(newWord);
                System.out.println("\u001B[33m##################################");
                System.out.println("##  言葉ー[" + kanji + "]");
                System.out.println("##  意味ー" + imi);
                System.out.println("##################################");

            } else {
                System.out.println("\u001B[31m間違っている、もう一度入力してください！");
            }

            if (newWord.endsWith("ん")) {
                System.out.println("Game over!");
                break;
            }
        }
        scanner.close();
    }

    public static boolean Validation(char oldWorldLastCharacter, char newWordFirstCharacter, ArrayList<String> lists, String newWord) throws MalformedURLException {
        boolean ValidationCheck = false;
//        ArrayList<String> jishoSearchReturn = JishoSearch(URLEncoder.encode(newWord));
        if (oldWorldLastCharacter == newWordFirstCharacter && newWord.length() > 2 && test) {
            ValidationCheck = true;
            oldWord = newWord;
            furigana=Character.UnicodeBlock.of(furigana.charAt(0)).equals(Character.UnicodeBlock.KATAKANA) ? convertKatakanatoHiragana(furigana) : furigana;
            oldWord = furigana;
//            oldWord = jishoSearchReturn.get(2);
        }
        if (newWord.length() < 3) {
            ValidationCheck = false;
            System.out.println("\u001B[31m二文字以上書いてください！");
        }
        for (String i : lists) {
            if (i.equals(newWord)) {
                System.out.println("\u001B[31m同じ言葉を書かないでください！");
                ValidationCheck = false;
            }
        }
        return ValidationCheck;
    }

    public static String convertKatakanatoHiragana(String katakana) {
        char[] katakanaArray = katakana.toCharArray();
        StringBuilder hiraganaBuilder = new StringBuilder();

        for (char katakanaChar : katakanaArray) {
            // Convert Hiragana to Katakana by adding the difference in Ascii values
            if (katakanaChar == 'ー') {
                hiraganaBuilder.append(katakanaChar);
                continue;
            }
            char hiraganaChar = (char) (katakanaChar - 'ァ' + 'ぁ');
            hiraganaBuilder.append(hiraganaChar);
        }
        return hiraganaBuilder.toString();

    }

    public static String RomajiToHiraganaConverter(String userInput) {
        StringBuilder sb = new StringBuilder();
        int x = 0;
        int y = 3;
        String initValue = userInput.substring(x, y);
        try {
            while (true) {
                if (search(initValue, sb)) {
                    x += initValue.length();
                    int s = y + 3;
                    if (s < userInput.length()) {
                        y += 3;
                    } else {
                        y = userInput.length();
                    }
                    initValue = userInput.substring(x, y);
                } else {
                    y -= 1;
                    initValue = userInput.substring(x, y);
                }
                if (x == userInput.length()) {
                    break;
                }
            }
            return sb.toString();
        } catch (StringIndexOutOfBoundsException e) {
            return "error";
        }
    }

    public static boolean search(String key, StringBuilder sb) {

        HashMap<String, String> moji_1 = new HashMap<>();
        moji_1.put("a", "あ");
        moji_1.put("i", "い");
        moji_1.put("u", "う");
        moji_1.put("e", "え");
        moji_1.put("o", "お");
        moji_1.put("ka", "か");
        moji_1.put("ki", "き");
        moji_1.put("ku", "く");
        moji_1.put("ke", "け");
        moji_1.put("ko", "こ");
        moji_1.put("kya", "きゃ");
        moji_1.put("kyu", "きゅ");
        moji_1.put("kyo", "きょ");
        moji_1.put("ga", "が");
        moji_1.put("gi", "ぎ");
        moji_1.put("gu", "ぐ");
        moji_1.put("ge", "げ");
        moji_1.put("go", "ご");
        moji_1.put("gya", "ぎゃ");
        moji_1.put("gyu", "ぎゅ");
        moji_1.put("gyo", "ぎょ");
        moji_1.put("sa", "さ");
        moji_1.put("shi", "し");
        moji_1.put("su", "す");
        moji_1.put("se", "せ");
        moji_1.put("so", "そ");
        moji_1.put("sha", "しゃ");
        moji_1.put("shu", "しゅ");
        moji_1.put("sho", "しょ");
        moji_1.put("za", "ざ");
        moji_1.put("ji", "じ");
        moji_1.put("zu", "ず");
        moji_1.put("ze", "ぜ");
        moji_1.put("zo", "ぞ");
        moji_1.put("ja", "じゃ");
        moji_1.put("ju", "じゅ");
        moji_1.put("jo", "じょ");
        moji_1.put("ta", "た");
        moji_1.put("chi", "ち");
        moji_1.put("tsu", "つ");
        moji_1.put("te", "て");
        moji_1.put("to", "と");
        moji_1.put("cha", "ちゃ");
        moji_1.put("chu", "ちゅ");
        moji_1.put("cho", "ちょ");
        moji_1.put("da", "だ");
        moji_1.put("ji", "じ");
        moji_1.put("zu", "ず");
        moji_1.put("de", "で");
        moji_1.put("do", "ど");
        moji_1.put("na", "な");
        moji_1.put("ni", "に");
        moji_1.put("nu", "ぬ");
        moji_1.put("ne", "ね");
        moji_1.put("no", "の");
        moji_1.put("nya", "にゃ");
        moji_1.put("nyu", "にゅ");
        moji_1.put("nyo", "にょ");
        moji_1.put("ha", "は");
        moji_1.put("hi", "ひ");
        moji_1.put("hu", "ふ");
        moji_1.put("he", "へ");
        moji_1.put("ho", "ほ");
        moji_1.put("hya", "ひゃ");
        moji_1.put("hyu", "ひゅ");
        moji_1.put("hyo", "ひょ");
        moji_1.put("ba", "ば");
        moji_1.put("bi", "び");
        moji_1.put("bu", "ぶ");
        moji_1.put("be", "べ");
        moji_1.put("bo", "ぼ");
        moji_1.put("bya", "びゃ");
        moji_1.put("byu", "びゅ");
        moji_1.put("byo", "びょ");
        moji_1.put("ma", "ま");
        moji_1.put("mi", "み");
        moji_1.put("mu", "む");
        moji_1.put("me", "め");
        moji_1.put("mo", "も");
        moji_1.put("mya", "みゃ");
        moji_1.put("myu", "みゅ");
        moji_1.put("myo", "みょ");
        moji_1.put("pa", "ぱ");
        moji_1.put("pi", "ぴ");
        moji_1.put("pu", "ぷ");
        moji_1.put("pe", "ぺ");
        moji_1.put("po", "ぽ");
        moji_1.put("pya", "ぴゃ");
        moji_1.put("pyu", "ぴゅ");
        moji_1.put("pyo", "ぴょ");
        moji_1.put("ya", "や");
        moji_1.put("yu", "ゆ");
        moji_1.put("yo", "よ");
        moji_1.put("ra", "ら");
        moji_1.put("ri", "り");
        moji_1.put("ru", "る");
        moji_1.put("re", "れ");
        moji_1.put("ro", "ろ");
        moji_1.put("rya", "りゃ");
        moji_1.put("ryu", "りゅ");
        moji_1.put("ryo", "りょ");
        moji_1.put("n", "ん");
        moji_1.put("wo", "を");
        moji_1.put("wa", "わ");
        for (String j : moji_1.keySet()) {
            if (j.equals(key)) {
                sb.append(moji_1.get(j));
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> JishoSearch(String keywords) throws MalformedURLException {
        ArrayList<String> returnValue = new ArrayList<>();
        try {
            URL link = new URL("https://jisho.org/api/v1/search/words?keyword=" + keywords);
            HttpURLConnection conn = (HttpURLConnection) link.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int ResponseCode = conn.getResponseCode();
            if (ResponseCode != 200) {
                throw new RuntimeException(String.valueOf(ResponseCode));
            } else {
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(link.openStream());
                while (scanner.hasNext()) {
                    sb.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray.length() != 0) {
                    JSONObject items = jsonArray.getJSONObject(0);

                    JSONArray japaneseArray = items.getJSONArray("senses");
                    JSONObject japaneseObj = japaneseArray.getJSONObject(0);
                    JSONArray test = japaneseObj.getJSONArray("english_definitions");
                    returnValue.add(test.toString());


                    //test start
                    JSONArray testArray = items.getJSONArray("japanese");
                    JSONObject testObj = testArray.getJSONObject(0);
                    if (testObj.length() == 2) {
                        String kanji = testObj.getString("word");
                        returnValue.add(kanji);
                    } else {
                        returnValue.add("error");
                    }
                    String furigana = testObj.getString("reading");
                    returnValue.add(furigana);
                }
                returnValue.add("error");
//                System.out.println("Testing"+ttt);

//                JSONObject testTest = (JSONObject) testObj.get("word");
//                System.out.println("kanji "+testObj);
                //test end
//                return returnValue;

            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("\u001B[33m##################################");
//        System.out.println("##  言葉ー[" + returnValue.get(1) + "]");
//        System.out.println("##  意味ー" + returnValue.get(0));
//        System.out.println("##################################");
//        oldWord=returnValue.get(2);
        return returnValue;

    }
}
