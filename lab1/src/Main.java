void main(String[] args) {
    if (args.length != 1) {
        IO.println("Wrong number of parameters. Enter filename.");
        return;
    }

    String inputFileName = args[0];
    String outputFileName = "output.csv";
    Set<WordStatistics> wordsStatisticsSet = new HashSet<>();

    try (Reader reader = new InputStreamReader(new FileInputStream(inputFileName))) {
        StringBuilder currentWord = new StringBuilder();
        int charCode;

        while ((charCode = reader.read()) != -1) {
            char symbol = (char) charCode;

            if (Character.isLetterOrDigit(symbol)) {
                currentWord.append(symbol);
            } else {
                if (!currentWord.isEmpty()) {
                    String word = currentWord.toString().toLowerCase();
                    addWordToSet(wordsStatisticsSet, word);
                    currentWord.setLength(0);
                }
            }
        }

        if (!currentWord.isEmpty()) {
            String word = currentWord.toString().toLowerCase();
            addWordToSet(wordsStatisticsSet, word);
        }

    } catch (IOException e) {
        System.err.println("Error while reading file: " + e.getLocalizedMessage());
        return;
    }

    List<WordStatistics> sortedWordStatisticsList = new ArrayList<>(wordsStatisticsSet);
    sortedWordStatisticsList.sort(new Comparator<>() { // передаю компаратор и переопределяю его для класса WordStatistics
        @Override
        public int compare(WordStatistics word1, WordStatistics word2) {
            return Integer.compare(word2.getCount(), word1.getCount());
        }
    });

    int totalWords = 0;
    for (WordStatistics w : sortedWordStatisticsList) {
        totalWords += w.getCount();
    }

    try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFileName)))
    {
        writer.write("Слово,Частота,Частота(%)\n");

        for (WordStatistics w : sortedWordStatisticsList) {
            String word = w.getWord();
            int count = w.getCount();
            double percentage = (count * 100.0) / totalWords;

            String line = String.format("%s,%d,%.2f%%\n", word, count, percentage);
            writer.write(line);
        }
    } catch (IOException e) {
        System.err.println("Can't write to file: " + outputFileName);
    }
}

static void addWordToSet(Set<WordStatistics> wordsStatisticsSet, String word) {
    WordStatistics temp = new WordStatistics(word);

    if (wordsStatisticsSet.contains(temp)) {
        for (WordStatistics w : wordsStatisticsSet) {
            if (w.equals(temp)) {
                w.addWord();
                break;
            }
        }
    } else {
        wordsStatisticsSet.add(temp);
    }
}