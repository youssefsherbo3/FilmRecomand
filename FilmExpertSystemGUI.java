import java.awt.*;
import java.io.*;
import java.util.concurrent.*;
import javax.swing.*;

public class FilmExpertSystemGUI {
    private JFrame frame;
    private JPanel mainPanel;
    private JComboBox<String> genreComboBox;
    private JComboBox<String> actorComboBox;
    private JComboBox<String> durationComboBox;
    private JComboBox<String> ratingComboBox;
    private JComboBox<String> yearComboBox;
    private JTextArea resultArea;
    private Process prologProcess;
    private BufferedReader prologInput;
    private BufferedWriter prologOutput;

    public FilmExpertSystemGUI() {
        initialize();
        connectToProlog();
    }

    private void initialize() {
        frame = new JFrame("Film Expert System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        // Genre selection
        inputPanel.add(new JLabel("Preferred Genre:"));
        String[] genres = {"Any", "Action", "Comedy", "Drama", "Sci-Fi", "Horror", "Romance", "Thriller", "Animation"};
        genreComboBox = new JComboBox<>(genres);
        inputPanel.add(genreComboBox);
        
        // Actor selection
        inputPanel.add(new JLabel("Preferred Actor:"));
        String[] actors = {"Any", "Tom Hanks", "Meryl Streep", "Leonardo DiCaprio", "Scarlett Johansson", "Denzel Washington"};
        actorComboBox = new JComboBox<>(actors);
        inputPanel.add(actorComboBox);
        
        // Duration selection
        inputPanel.add(new JLabel("Duration:"));
        String[] durations = {"Any", "<90 min", "90-120 min", ">120 min"};
        durationComboBox = new JComboBox<>(durations);
        inputPanel.add(durationComboBox);
        
        // Rating selection
        inputPanel.add(new JLabel("Minimum Rating:"));
        String[] ratings = {"Any", "7+", "8+", "9+"};
        ratingComboBox = new JComboBox<>(ratings);
        inputPanel.add(ratingComboBox);
        
        // Year selection
        inputPanel.add(new JLabel("Release Year:"));
        String[] years = {"Any", "2000s", "2010s", "2020s", "Before 2000"};
        yearComboBox = new JComboBox<>(years);
        inputPanel.add(yearComboBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton recommendButton = new JButton("Get Recommendations");
        recommendButton.addActionListener(e -> getRecommendations());
        buttonPanel.add(recommendButton);
        
        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        // Add components to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private void connectToProlog() {
        try {
            // Start Prolog process (adjust path to your Prolog executable)
            prologProcess = Runtime.getRuntime().exec("swipl -s films.pl");
            
            prologInput = new BufferedReader(new InputStreamReader(prologProcess.getInputStream()));
            prologOutput = new BufferedWriter(new OutputStreamWriter(prologProcess.getOutputStream()));
            
            // Wait for Prolog to initialize
            TimeUnit.SECONDS.sleep(2);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to Prolog: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void getRecommendations() {
        try {
            // Get user selections
            String genre = (String) genreComboBox.getSelectedItem();
            String actor = (String) actorComboBox.getSelectedItem();
            String duration = (String) durationComboBox.getSelectedItem();
            String rating = (String) ratingComboBox.getSelectedItem();
            String year = (String) yearComboBox.getSelectedItem();
            
            // Build Prolog query
            StringBuilder query = new StringBuilder("recommend_film(");
            
            query.append(genre.equals("Any") ? "_" : "'" + genre + "'").append(", ");
            query.append(actor.equals("Any") ? "_" : "'" + actor + "'").append(", ");
            
            // Handle duration
            if (duration.equals("Any")) {
                query.append("_, ");
            } else if (duration.equals("<90 min")) {
                query.append("short, ");
            } else if (duration.equals("90-120 min")) {
                query.append("medium, ");
            } else {
                query.append("long, ");
            }
            
            // Handle rating
            if (rating.equals("Any")) {
                query.append("_, ");
            } else {
                query.append(rating.substring(0, 1)).append(", ");
            }
            
            // Handle year
            if (year.equals("Any")) {
                query.append("_");
            } else if (year.equals("2000s")) {
                query.append("between(2000, 2009)");
            } else if (year.equals("2010s")) {
                query.append("between(2010, 2019)");
            } else if (year.equals("2020s")) {
                query.append("between(2020, 2029)");
            } else {
                query.append("before(2000)");
            }
            
            query.append(", Film).");
            
            // Send query to Prolog
            prologOutput.write(query.toString() + "\n");
            prologOutput.flush();
            
            // Read response
            resultArea.setText("Recommended Films:\n\n");
            String line;
            while ((line = prologInput.readLine()) != null) {
                if (line.trim().equals("false.")) {
                    resultArea.append("No films match your criteria.\n");
                    break;
                }
                if (line.startsWith("Film = ")) {
                    String filmName = line.substring(8, line.length() - 1);
                    resultArea.append("- " + filmName + "\n");
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error getting recommendations: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FilmExpertSystemGUI());
    }
}