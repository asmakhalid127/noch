package com.noch.controllers;

import com.noch.models.User;
import com.noch.service.ReviewService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AdminProductReviewsController implements Initializable {

    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Label productDescLabel;
    @FXML private Label productImageLabel;
    @FXML private HBox productStarsBox;
    @FXML private Label productRatingLabel;
    @FXML private Label totalReviewsLabel;
    @FXML private Label flaggedCountLabel;
    @FXML private Label recentCountLabel;
    @FXML private FlowPane filterRow;
    @FXML private TextField searchField;
    @FXML private VBox reviewsList;

    private User currentUser;
    private String[] product;
    private List<String[]> reviews = new ArrayList<>();
    private String activeFilter = "all";
    private final ReviewService reviewService = new ReviewService();
    private static final String IMG = "src/main/resources/images/";

    private static final Map<String, String[]> PRODUCTS = new LinkedHashMap<>();
    static {
        PRODUCTS.put("1", new String[]{"1","NOCH SATIN EVENING DRESS","£142.00","Elegant emerald satin evening dress with a sophisticated silhouette. Deep V-back, floor-length cut.",       IMG+"product1.jpg"});
        PRODUCTS.put("2", new String[]{"2","NOCH LINEN SHIRT",         "£59.99", "Breathable pure linen shirt with a loose, relaxed silhouette. Perfect for all seasons.",                   IMG+"product2.jpg"});
        PRODUCTS.put("3", new String[]{"3","NOCH SLIM TROUSERS",       "£69.99", "Tailored slim-fit trousers in stretch cotton. Smart enough for the office, easy enough for every day.",    IMG+"product3.jpg"});
        PRODUCTS.put("4", new String[]{"4","NOCH WIDE LEG JEANS",      "£74.99", "Wide-leg denim in a clean indigo wash. High waist, straight cut, timeless.",                               IMG+"product4.jpg"});
        PRODUCTS.put("5", new String[]{"5","NOCH OVERSHIRT JACKET",    "£89.99", "A structured overshirt jacket in brushed cotton twill. Wear open or buttoned as a light layer.",           IMG+"product5.jpg"});
        PRODUCTS.put("6", new String[]{"6","NOCH WOOL COAT",           "£149.99","A minimalist single-breasted wool blend coat. Longline cut, clean finish, cold weather essential.",        IMG+"product6.jpg"});
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void setUser(User user) { this.currentUser = user; }

    public void setProduct(String productId, String productName) {
        this.product = PRODUCTS.getOrDefault(productId,
            new String[]{productId, productName, "£0.00", "No description.", null});

        productNameLabel.setText(product[1]);
        productPriceLabel.setText(product[2]);
        productDescLabel.setText(product[3]);

        // Show image or fallback letter
        if (product[4] != null) {
            File imgFile = new File(product[4]);
            if (imgFile.exists()) {
                try {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(96); iv.setFitHeight(96); iv.setPreserveRatio(true);
                    productImageLabel.setText("");
                    productImageLabel.setGraphic(iv);
                } catch (Exception e) { productImageLabel.setText(String.valueOf(product[1].charAt(0))); }
            } else { productImageLabel.setText(String.valueOf(product[1].charAt(0))); }
        } else { productImageLabel.setText(String.valueOf(product[1].charAt(0))); }

        reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId));
        updateStats(); buildFilterButtons(); renderReviews();
    }

    private void updateStats() {
        totalReviewsLabel.setText(String.valueOf(reviews.size()));
        long flagged = reviews.stream().filter(r -> "1".equals(r[5]) || "true".equals(r[5])).count();
        flaggedCountLabel.setText(String.valueOf(flagged));
        recentCountLabel.setText(String.valueOf(Math.min(reviews.size(), 5)));
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(r -> Integer.parseInt(r[3])).average().orElse(0);
            updateStarsDisplay(productStarsBox, (int) Math.round(avg));
            if (productRatingLabel != null) productRatingLabel.setText(String.format("%.1f (%d reviews)", avg, reviews.size()));
        }
    }

    private void buildFilterButtons() {
        filterRow.getChildren().clear();
        long flaggedCount = reviews.stream().filter(r -> "1".equals(r[5]) || "true".equals(r[5])).count();
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("all",     "All (" + reviews.size() + ")");
        filters.put("flagged", "⚑ Flagged (" + flaggedCount + ")");
        filters.put("5","★★★★★"); filters.put("4","★★★★"); filters.put("3","★★★"); filters.put("2","★★"); filters.put("1","★");
        filters.put("recent","Recent"); filters.put("helpful","Most Helpful");
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            Button btn = new Button(entry.getValue());
            boolean isActive = activeFilter.equals(entry.getKey());
            btn.setStyle("-fx-border-color:"+(isActive?"black":"#d1d5db")+";-fx-border-width:1;-fx-background-color:"+(isActive?"black":"white")+";-fx-text-fill:"+(isActive?"white":"black")+";-fx-font-size:12px;-fx-padding:6 14;-fx-cursor:hand;");
            btn.setOnAction(e -> { activeFilter = entry.getKey(); buildFilterButtons(); renderReviews(); });
            filterRow.getChildren().add(btn);
        }
    }

    private void renderReviews() {
        reviewsList.getChildren().clear();
        List<String[]> filtered = new ArrayList<>(reviews);
        switch (activeFilter) {
            case "flagged": filtered = filtered.stream().filter(r -> "1".equals(r[5]) || "true".equals(r[5])).collect(Collectors.toList()); break;
            case "5": case "4": case "3": case "2": case "1":
                int star = Integer.parseInt(activeFilter);
                filtered = filtered.stream().filter(r -> Integer.parseInt(r[3]) == star).collect(Collectors.toList()); break;
            case "recent":
                filtered.sort((a,b) -> { String da=a.length>8?a[8]:""; String db=b.length>8?b[8]:""; return db.compareTo(da); });
                filtered = filtered.stream().limit(10).collect(Collectors.toList()); break;
            case "helpful": filtered.sort((a,b) -> Integer.parseInt(b[4]) - Integer.parseInt(a[4])); break;
            default: break;
        }
        if (searchField != null && searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
            String q = searchField.getText().toLowerCase();
            filtered = filtered.stream().filter(r -> r[1].toLowerCase().contains(q) || r[2].toLowerCase().contains(q)).collect(Collectors.toList());
        }
        if (filtered.isEmpty()) {
            Label empty = new Label(reviews.isEmpty() ? "No reviews for this product yet." : "No reviews match the current filter.");
            empty.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;-fx-padding:32;");
            reviewsList.getChildren().add(empty); return;
        }
        for (String[] review : filtered) reviewsList.getChildren().add(createAdminReviewCard(review));
    }

    private VBox createAdminReviewCard(String[] review) {
        boolean isFlagged = "1".equals(review[5]) || "true".equals(review[5]);
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-border-color:"+(isFlagged?"#ef4444":"#e5e7eb")+";-fx-border-width:"+(isFlagged?"2":"1")+";-fx-background-color:"+(isFlagged?"#fff0f0":"white")+";");

        VBox viewMode = new VBox(10);
        HBox header = new HBox(12); header.setAlignment(Pos.CENTER_LEFT);
        HBox stars = new HBox(2); updateStarsDisplay(stars, Integer.parseInt(review[3]));
        Label nameLabel = new Label(review[1]); nameLabel.setStyle("-fx-font-weight:bold;-fx-font-size:13px;");
        String date = review.length > 8 && review[8] != null ? review[8].substring(0, 10) : "";
        Label dateLabel = new Label(date); dateLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(stars, nameLabel, dateLabel, spacer);
        if (isFlagged) { Label fb = new Label("⚑ FLAGGED"); fb.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-font-size:10px;-fx-padding:3 8;-fx-font-weight:bold;"); header.getChildren().add(fb); }

        Label commentLabel = new Label(review[2]); commentLabel.setWrapText(true); commentLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#4b5563;");

        HBox statsRow = new HBox(16); statsRow.setAlignment(Pos.CENTER_LEFT);
        Label helpfulLbl = new Label("👍 " + review[4] + "  👎 " + (review.length > 11 ? review[11] : "0"));
        helpfulLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;"); statsRow.getChildren().add(helpfulLbl);

        HBox btnRow = new HBox(10); btnRow.setAlignment(Pos.CENTER_LEFT);
        Button flagBtn = new Button(isFlagged ? "⚑ Unflag" : "⚑ Flag");
        flagBtn.setStyle("-fx-border-color:"+(isFlagged?"#ef4444":"black")+";-fx-border-width:1;-fx-background-color:"+(isFlagged?"#ef4444":"white")+";-fx-text-fill:"+(isFlagged?"white":"black")+";-fx-font-size:12px;-fx-padding:6 14;-fx-cursor:hand;");
        flagBtn.setOnAction(e -> { reviewService.setFlagged(review[0], !isFlagged); reviews = new ArrayList<>(reviewService.getReviewsByProductId(product[0])); updateStats(); buildFilterButtons(); renderReviews(); });

        Button editBtn = new Button("✎ Edit");
        editBtn.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-font-size:12px;-fx-padding:6 14;-fx-cursor:hand;");
        Button deleteBtn = new Button("✕ Delete");
        deleteBtn.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-font-size:12px;-fx-padding:6 14;-fx-cursor:hand;");
        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION); alert.setTitle("Delete Review"); alert.setHeaderText(null); alert.setContentText("Are you sure you want to delete this review?");
            alert.showAndWait().ifPresent(result -> { if (result == ButtonType.OK) { reviewService.deleteReview(review[0]); reviews = new ArrayList<>(reviewService.getReviewsByProductId(product[0])); updateStats(); buildFilterButtons(); renderReviews(); } });
        });
        btnRow.getChildren().addAll(flagBtn, editBtn, deleteBtn);
        viewMode.getChildren().addAll(header, commentLabel, statsRow, btnRow);

        VBox editMode = new VBox(12); editMode.setVisible(false); editMode.setManaged(false);
        Label editTitle = new Label("EDIT REVIEW"); editTitle.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-letter-spacing:2px;");
        HBox editStarRow = new HBox(8); editStarRow.setAlignment(Pos.CENTER_LEFT);
        final int[] editRating = {Integer.parseInt(review[3])};
        Label editRatingText = new Label(editRating[0] + " star" + (editRating[0] != 1 ? "s" : "")); editRatingText.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        for (int i=1;i<=5;i++) {
            final int val=i; Label star=new Label("★");
            star.setStyle("-fx-font-size:28px;-fx-text-fill:"+(i<=editRating[0]?"black":"#d1d5db")+";-fx-cursor:hand;");
            star.setOnMouseEntered(ev -> { for (int j=0;j<editStarRow.getChildren().size()-1;j++) ((Label)editStarRow.getChildren().get(j)).setStyle("-fx-font-size:28px;-fx-text-fill:"+(j<val?"black":"#d1d5db")+";-fx-cursor:hand;"); });
            star.setOnMouseExited(ev ->  { for (int j=0;j<editStarRow.getChildren().size()-1;j++) ((Label)editStarRow.getChildren().get(j)).setStyle("-fx-font-size:28px;-fx-text-fill:"+(j<editRating[0]?"black":"#d1d5db")+";-fx-cursor:hand;"); });
            star.setOnMouseClicked(ev -> { editRating[0]=val; editRatingText.setText(val+" star"+(val!=1?"s":"")); for (int j=0;j<editStarRow.getChildren().size()-1;j++) ((Label)editStarRow.getChildren().get(j)).setStyle("-fx-font-size:28px;-fx-text-fill:"+(j<val?"black":"#d1d5db")+";-fx-cursor:hand;"); });
            editStarRow.getChildren().add(star);
        }
        editStarRow.getChildren().add(editRatingText);
        TextArea editArea = new TextArea(review[2]); editArea.setPrefRowCount(4); editArea.setWrapText(true); editArea.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-font-size:13px;");
        Label editErrorLbl = new Label(""); editErrorLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;-fx-background-color:#f3f4f6;-fx-padding:8;-fx-border-color:#d1d5db;-fx-border-width:1;"); editErrorLbl.setVisible(false); editErrorLbl.setManaged(false);
        HBox editBtnRow = new HBox(10);
        Button saveBtn = new Button("SAVE"); saveBtn.setStyle("-fx-background-color:black;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:7 20;-fx-cursor:hand;");
        saveBtn.setOnAction(e -> {
            String nc = editArea.getText().trim(); editErrorLbl.setVisible(false); editErrorLbl.setManaged(false);
            if (editRating[0]<1) { editErrorLbl.setText("Please select a rating."); editErrorLbl.setVisible(true); editErrorLbl.setManaged(true); return; }
            if (nc.isEmpty()) { editErrorLbl.setText("Comment cannot be empty."); editErrorLbl.setVisible(true); editErrorLbl.setManaged(true); return; }
            if (nc.matches(".*[@,.#/\\\\?\"'~`$].*")) { editErrorLbl.setText("Comment contains invalid characters."); editErrorLbl.setVisible(true); editErrorLbl.setManaged(true); return; }
            reviewService.updateReview(review[0], editRating[0], nc);
            reviews = new ArrayList<>(reviewService.getReviewsByProductId(product[0])); updateStats(); renderReviews();
        });
        Button cancelEditBtn = new Button("CANCEL"); cancelEditBtn.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-font-size:12px;-fx-padding:7 20;-fx-cursor:hand;");
        cancelEditBtn.setOnAction(e -> { editMode.setVisible(false); editMode.setManaged(false); viewMode.setVisible(true); viewMode.setManaged(true); });
        editBtnRow.getChildren().addAll(saveBtn, cancelEditBtn);
        editMode.getChildren().addAll(editTitle, editStarRow, editArea, editErrorLbl, editBtnRow);
        editBtn.setOnAction(e -> { viewMode.setVisible(false); viewMode.setManaged(false); editMode.setVisible(true); editMode.setManaged(true); });
        card.getChildren().addAll(viewMode, editMode);
        return card;
    }

    private void updateStarsDisplay(HBox box, int rating) {
        box.getChildren().clear();
        for (int i=1;i<=5;i++) { Label s=new Label("★"); s.setStyle(i<=rating?"-fx-text-fill:black;-fx-font-size:14px;":"-fx-text-fill:#d1d5db;-fx-font-size:14px;"); box.getChildren().add(s); }
    }

    @FXML private void handleSearch() { renderReviews(); }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            AdminDashboardController ctrl = loader.getController();
            if (currentUser != null) ctrl.setUser(currentUser);
            Stage stage = (Stage) productNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800)); stage.setTitle("NOCH – Admin Dashboard");
        } catch (IOException e) { e.printStackTrace(); }
    }
}