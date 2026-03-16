public class WordStatistics {
    final private String word;
    private int count;

    public WordStatistics(String word) {
        this.word = word;
        this.count = 1;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    public void addWord(){
        count++;
    }

    @Override public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WordStatistics otherClass = (WordStatistics) obj;
        return word.equals(otherClass.word);
    }
    @Override public int hashCode(){
        return word.hashCode();
    }
}
