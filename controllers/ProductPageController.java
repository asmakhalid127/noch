package com.noch.controllers;

import com.noch.db.DBInitializer;
import com.noch.models.User;
import com.noch.service.ReviewService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProductPageController implements Initializable {

    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Label productDescription;
    @FXML private Label imageLabel;
    @FXML private HBox starsBox;
    @FXML private Label ratingLabel;
    @FXML private HBox ratingBox;
    @FXML private Button addReviewBtn;
    @FXML private Label reviewPermissionMsg;
    @FXML private VBox reviewsList;
    @FXML private HBox topReviewBox;
    @FXML private TextField reviewSearchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> filterCombo;

    private User currentUser;
    private String productId;
    private List<String[]> reviews = new ArrayList<>();
    private final ReviewService reviewService = new ReviewService();
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String IMG = "src/main/resources/images/";

    private static final Map<String, String[]> PRODUCTS = new LinkedHashMap<>();
    static {
        // {name, price, description, imagePath}
        PRODUCTS.put("1", new String[]{"NOCH SATIN EVENING DRESS", "£142.00", "Elegant emerald satin evening dress with a sophisticated silhouette. Deep V-back, floor-length cut, premium satin fabric.", IMG + "product1.jpg"});
        PRODUCTS.put("2", new String[]{"NOCH LINEN SHIRT",          "£59.99",  "Breathable pure linen shirt with a loose, relaxed silhouette. Perfect for all seasons.",                                    IMG + "product2.jpg"});
        PRODUCTS.put("3", new String[]{"NOCH SLIM TROUSERS",        "£69.99",  "Tailored slim-fit trousers in stretch cotton. Smart enough for the office, easy enough for every day.",                    IMG + "product3.jpg"});
        PRODUCTS.put("4", new String[]{"NOCH WIDE LEG JEANS",       "£74.99",  "Wide-leg denim in a clean indigo wash. High waist, straight cut, timeless.",                                               IMG + "product4.jpg"});
        PRODUCTS.put("5", new String[]{"NOCH OVERSHIRT JACKET",     "£89.99",  "A structured overshirt jacket in brushed cotton twill. Wear open or buttoned as a light layer.",                           IMG + "product5.jpg"});
        PRODUCTS.put("6", new String[]{"NOCH WOOL COAT",            "£149.99", "A minimalist single-breasted wool blend coat. Longline cut, clean finish, cold weather essential.",                        IMG + "product6.jpg"});
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sortCombo != null) {
            sortCombo.getItems().addAll("Newest First","Oldest First","Highest Rating","Lowest Rating","Most Helpful");
            sortCombo.setValue("Newest First");
            sortCombo.setOnAction(e -> renderReviews());
        }
        if (filterCombo != null) {
            filterCombo.getItems().addAll("All Ratings","5 Stars","4 Stars","3 Stars","2 Stars","1 Star");
            filterCombo.setValue("All Ratings");
            filterCombo.setOnAction(e -> renderReviews());
        }
    }

    public void setProduct(String id, User user) {
        this.productId = id;
        this.currentUser = user;
        String[] p = PRODUCTS.getOrDefault(id, new String[]{"Unknown","£0.00","No description.", null});
        productName.setText(p[0]);
        productPrice.setText(p[1]);
        productDescription.setText(p[2]);

        // Show image if available, otherwise show letter
        File imgFile = p[3] != null ? new File(p[3]) : null;
        if (imgFile != null && imgFile.exists()) {
            imageLabel.setText("");
            imageLabel.setGraphic(makeProductImageView(imgFile, 460, 460));
        } else {
            imageLabel.setText(String.valueOf(p[0].charAt(0)));
        }

        reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId));
        updateReviewButton();
        renderTopRated();
        renderReviews();
    }

    private ImageView makeProductImageView(File file, double w, double h) {
        try {
            ImageView iv = new ImageView(new Image(file.toURI().toString()));
            iv.setFitWidth(w); iv.setFitHeight(h);
            iv.setPreserveRatio(true); iv.setSmooth(true);
            return iv;
        } catch (Exception e) { return null; }
    }

    // ── TOP RATED ─────────────────────────────────────────────────────────

    private void renderTopRated() {
        if (topReviewBox == null) return;
        topReviewBox.getChildren().clear();
        List<String[]> top = reviews.stream()
            .filter(r -> Integer.parseInt(r[3]) >= 4)
            .sorted((a, b) -> Integer.parseInt(b[4]) - Integer.parseInt(a[4]))
            .limit(3).collect(Collectors.toList());
        if (top.isEmpty()) return;

        VBox section = new VBox(8);
        section.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-border-color:#e5e7eb;-fx-border-width:1;-fx-padding:16;-fx-background-color:#f9fafb;");
        Label title = new Label("★ TOP RATED REVIEWS");
        title.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-letter-spacing:2px;");
        section.getChildren().add(title);

        for (String[] r : top) {
            HBox card = new HBox(12);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb;-fx-border-width:1;-fx-padding:10;");
            HBox stars = new HBox(2);
            starsOf(stars, Integer.parseInt(r[3]), 12);
            VBox content = new VBox(2);
            HBox.setHgrow(content, Priority.ALWAYS);
            Label name = new Label(r[1]);
            name.setStyle("-fx-font-weight:bold;-fx-font-size:12px;");
            Label cmt = new Label(r[2].length() > 90 ? r[2].substring(0, 90) + "..." : r[2]);
            cmt.setWrapText(true);
            cmt.setStyle("-fx-font-size:12px;-fx-text-fill:#4b5563;");
            content.getChildren().addAll(name, cmt);
            Label helpful = new Label("👍 " + r[4]);
            helpful.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");
            card.getChildren().addAll(stars, content, helpful);
            section.getChildren().add(card);
        }
        topReviewBox.getChildren().add(section);
    }

    // ── PERMISSION ────────────────────────────────────────────────────────

    private void updateReviewButton() {
        if (currentUser == null) { addReviewBtn.setDisable(true); showPerm("Please log in to submit a review."); return; }
        if (!currentUser.isCustomer()) { addReviewBtn.setDisable(true); showPerm("Only verified customers can submit reviews."); return; }
        if (currentUser.getPurchasedProducts() == null || !currentUser.getPurchasedProducts().contains(productId)) {
            addReviewBtn.setDisable(true); showPerm("You must purchase this product before reviewing."); return;
        }
        if (reviews.stream().anyMatch(r -> r[6].equals(currentUser.getId()))) {
            addReviewBtn.setDisable(true); showPerm("You have already reviewed this product.");
        } else {
            addReviewBtn.setDisable(false); hidePerm();
        }
    }

    // ── RENDER REVIEWS ────────────────────────────────────────────────────

    private void renderReviews() {
        reviewsList.getChildren().clear();
        List<String[]> list = new ArrayList<>(reviews);

        if (filterCombo != null && filterCombo.getValue() != null && !filterCombo.getValue().equals("All Ratings")) {
            int rf = Integer.parseInt(filterCombo.getValue().replace(" Stars","").replace(" Star",""));
            list = list.stream().filter(r -> Integer.parseInt(r[3]) == rf).collect(Collectors.toList());
        }
        if (reviewSearchField != null && reviewSearchField.getText() != null && !reviewSearchField.getText().trim().isEmpty()) {
            String q = reviewSearchField.getText().toLowerCase();
            list = list.stream().filter(r -> r[1].toLowerCase().contains(q) || r[2].toLowerCase().contains(q)).collect(Collectors.toList());
        }
        if (sortCombo != null && sortCombo.getValue() != null) {
            switch (sortCombo.getValue()) {
                case "Highest Rating": list.sort((a,b) -> Integer.parseInt(b[3]) - Integer.parseInt(a[3])); break;
                case "Lowest Rating":  list.sort((a,b) -> Integer.parseInt(a[3]) - Integer.parseInt(b[3])); break;
                case "Most Helpful":   list.sort((a,b) -> Integer.parseInt(b[4]) - Integer.parseInt(a[4])); break;
                default: break;
            }
        }

        if (reviews.isEmpty()) { ratingBox.setVisible(false); ratingBox.setManaged(false); }
        else {
            ratingBox.setVisible(true); ratingBox.setManaged(true);
            double avg = reviews.stream().mapToInt(r -> Integer.parseInt(r[3])).average().orElse(0);
            starsOf(starsBox, (int) Math.round(avg), 14);
            ratingLabel.setText(String.format("%.1f (%d %s)", avg, reviews.size(), reviews.size() == 1 ? "review" : "reviews"));
        }

        if (list.isEmpty()) {
            Label e = new Label(reviews.isEmpty() ? "No reviews yet. Be the first to review!" : "No reviews match your filters.");
            e.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;-fx-padding:32;");
            reviewsList.getChildren().add(e);
            return;
        }
        for (String[] r : list) reviewsList.getChildren().add(makeCard(r));
    }

    // ── REVIEW CARD ───────────────────────────────────────────────────────

    private VBox makeCard(String[] review) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color:#e5e7eb;-fx-border-width:1;-fx-padding:16;-fx-background-color:white;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameL = new Label(review[1]);
        nameL.setStyle("-fx-font-weight:bold;-fx-font-size:13px;");
        HBox stars = new HBox(2);
        starsOf(stars, Integer.parseInt(review[3]), 14);
        Label dateL = new Label(review[8] != null ? review[8].substring(0, 10) : "");
        dateL.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        HBox actions = new HBox(6);
        boolean isOwn = currentUser != null && review[6].equals(currentUser.getId());
        if (isOwn) {
            if (canEdit(review)) {
                Button editBtn = new Button("✎ Edit");
                editBtn.setStyle("-fx-font-size:11px;-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;");
                editBtn.setOnAction(e -> showEditDialog(review));
                Button delBtn = new Button("✕ Delete");
                delBtn.setStyle("-fx-font-size:11px;-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;");
                delBtn.setOnAction(e -> {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setTitle("Delete Review"); a.setHeaderText(null);
                    a.setContentText("Are you sure you want to delete your review?");
                    a.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.OK) {
                            reviewService.deleteReview(review[0]);
                            reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId));
                            updateReviewButton(); renderTopRated(); renderReviews();
                        }
                    });
                });
                Label timer = new Label();
                timer.setStyle("-fx-font-size:10px;-fx-text-fill:#f59e0b;-fx-font-weight:bold;");
                startTimer(review, timer, editBtn, delBtn);
                actions.getChildren().addAll(timer, editBtn, delBtn);
            } else {
                Label locked = new Label("🔒 Locked");
                locked.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
                actions.getChildren().add(locked);
            }
        }
        header.getChildren().addAll(nameL, stars, dateL, sp, actions);

        Label cmt = new Label(review[2]);
        cmt.setWrapText(true);
        cmt.setStyle("-fx-font-size:13px;-fx-text-fill:#4b5563;");

        VBox imgSection = new VBox();
        if (review[10] != null && !review[10].isEmpty()) {
            try {
                ImageView iv = new ImageView(new Image("file:" + review[10]));
                iv.setFitWidth(200); iv.setFitHeight(150); iv.setPreserveRatio(true);
                imgSection.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        String uid = currentUser != null ? currentUser.getId() : null;
        String vote = uid != null ? reviewService.getVote(uid, review[0]) : null;
        boolean liked = "like".equals(vote);
        boolean disliked = "dislike".equals(vote);

        HBox votes = new HBox(12);
        votes.setAlignment(Pos.CENTER_LEFT);
        Label wasHelpful = new Label("Was this helpful?");
        wasHelpful.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");

        Button likeBtn = new Button("👍 " + review[4]);
        likeBtn.setStyle("-fx-font-size:12px;-fx-border-color:" + (liked?"black":"#d1d5db") + ";-fx-border-width:1;-fx-background-color:" + (liked?"#f0f0f0":"white") + ";-fx-cursor:hand;");
        likeBtn.setDisable(currentUser == null);
        likeBtn.setOnAction(e -> { reviewService.castVote(currentUser.getId(), review[0], "like"); reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId)); renderTopRated(); renderReviews(); });

        Button dislikeBtn = new Button("👎 " + review[11]);
        dislikeBtn.setStyle("-fx-font-size:12px;-fx-border-color:" + (disliked?"black":"#d1d5db") + ";-fx-border-width:1;-fx-background-color:" + (disliked?"#f0f0f0":"white") + ";-fx-cursor:hand;");
        dislikeBtn.setDisable(currentUser == null);
        dislikeBtn.setOnAction(e -> { reviewService.castVote(currentUser.getId(), review[0], "dislike"); reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId)); renderTopRated(); renderReviews(); });

        boolean flagged = "1".equals(review[5]) || "true".equals(review[5]);
        Button flagBtn = new Button(flagged ? "⚑ Flagged" : "⚑ Flag");
        flagBtn.setStyle("-fx-font-size:12px;-fx-border-color:" + (flagged?"black":"#d1d5db") + ";-fx-border-width:1;-fx-background-color:" + (flagged?"black":"white") + ";-fx-text-fill:" + (flagged?"white":"black") + ";-fx-cursor:hand;");
        flagBtn.setDisable(currentUser == null);
        flagBtn.setOnAction(e -> { reviewService.setFlagged(review[0], !flagged); reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId)); renderReviews(); });

        votes.getChildren().addAll(wasHelpful, likeBtn, dislikeBtn, flagBtn);
        card.getChildren().addAll(header, cmt);
        if (!imgSection.getChildren().isEmpty()) card.getChildren().add(imgSection);
        card.getChildren().add(votes);
        return card;
    }

    // ── TIMER ─────────────────────────────────────────────────────────────

    private boolean canEdit(String[] review) {
        String eu = review[9];
        if (eu == null || eu.isEmpty()) return true;
        try { return LocalDateTime.now().isBefore(LocalDateTime.parse(eu, DB_FMT)); }
        catch (Exception e) { return false; }
    }

    private void startTimer(String[] review, Label lbl, Button editBtn, Button delBtn) {
        String eu = review[9];
        if (eu == null || eu.isEmpty()) { lbl.setVisible(false); lbl.setManaged(false); return; }
        try {
            LocalDateTime deadline = LocalDateTime.parse(eu, DB_FMT);
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                long s = java.time.Duration.between(LocalDateTime.now(), deadline).getSeconds();
                if (s <= 0) { lbl.setText("🔒 Locked"); lbl.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;"); editBtn.setDisable(true); delBtn.setDisable(true); }
                else { lbl.setText(String.format("⏱ %d:%02d", s/60, s%60)); }
            }));
            t.setCycleCount(Timeline.INDEFINITE); t.play();
        } catch (Exception ignored) {}
    }

    // ── ADD REVIEW DIALOG ─────────────────────────────────────────────────

    @FXML
    private void handleAddReview() {
        Stage dlg = new Stage();
        dlg.setTitle("Add Review");
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.initOwner(addReviewBtn.getScene().getWindow());

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:white;");
        root.getChildren().add(makeTitleBar("ADD REVIEW", dlg));

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));

        Label rLbl = new Label("RATING *"); rLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        HBox starRow = new HBox(8); starRow.setAlignment(Pos.CENTER_LEFT);
        int[] rating = {0};
        Label rText = new Label(""); rText.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;");
        buildStars(starRow, rating, rText, 0); starRow.getChildren().add(rText);

        Label cLbl = new Label("COMMENT *"); cLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        TextArea cArea = new TextArea();
        cArea.setPromptText("Share your experience with this product...");
        cArea.setPrefRowCount(5); cArea.setWrapText(true);
        cArea.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-font-size:13px;");
        Label noteLbl = new Label("Note: Special characters (@,.#/\\?\"'~`$) are not allowed.");
        noteLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");

        Label iLbl = new Label("PHOTO (optional)"); iLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        String[] imgPath = {null};
        HBox imgRow = makeImageRow("📷 Choose Image", imgPath, null, dlg);

        Label errLbl = makeErrorLabel();

        HBox btnRow = new HBox(12);
        Button cancel = makeBtn("CANCEL", false); cancel.setOnAction(e -> dlg.close());
        Button submit = makeBtn("SUBMIT REVIEW", true);
        submit.setOnAction(e -> {
            errLbl.setVisible(false); errLbl.setManaged(false);
            if (rating[0] < 1) { showErr(errLbl, "Please select a rating between 1 and 5 stars."); return; }
            String comment = cArea.getText().trim();
            if (comment.isEmpty()) { showErr(errLbl, "Please write a comment."); return; }
            if (comment.matches(".*[@,.#/\\\\?\"'~`$].*")) { showErr(errLbl, "Comment contains invalid characters."); return; }
            String editableUntil = LocalDateTime.now().plusMinutes(DBInitializer.REVIEW_EDIT_WINDOW_MINUTES).format(DB_FMT);
            reviewService.addReview("r" + System.currentTimeMillis(), productId,
                currentUser.getId(), currentUser.getName(), rating[0], comment, imgPath[0], editableUntil);
            reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId));
            updateReviewButton(); renderTopRated(); renderReviews();
            dlg.close();
        });
        HBox.setHgrow(cancel, Priority.ALWAYS); HBox.setHgrow(submit, Priority.ALWAYS);
        btnRow.getChildren().addAll(cancel, submit);
        form.getChildren().addAll(rLbl, starRow, cLbl, cArea, noteLbl, iLbl, imgRow, errLbl, btnRow);
        root.getChildren().add(form);
        dlg.setScene(new Scene(root, 520, 560)); dlg.showAndWait();
    }

    // ── EDIT REVIEW DIALOG ────────────────────────────────────────────────

    private void showEditDialog(String[] review) {
        Stage dlg = new Stage();
        dlg.setTitle("Edit Review");
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.initOwner(productName.getScene().getWindow());

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:white;");
        root.getChildren().add(makeTitleBar("EDIT REVIEW", dlg));

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));

        Label rLbl = new Label("RATING *"); rLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        HBox starRow = new HBox(8); starRow.setAlignment(Pos.CENTER_LEFT);
        int[] rating = {Integer.parseInt(review[3])};
        Label rText = new Label(rating[0] + " star" + (rating[0] != 1 ? "s" : ""));
        rText.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;");
        buildStars(starRow, rating, rText, rating[0]); starRow.getChildren().add(rText);

        Label cLbl = new Label("COMMENT *"); cLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        TextArea cArea = new TextArea(review[2]);
        cArea.setPrefRowCount(5); cArea.setWrapText(true);
        cArea.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-font-size:13px;");
        Label noteLbl = new Label("Note: Special characters (@,.#/\\?\"'~`$) are not allowed.");
        noteLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");

        Label iLbl = new Label("PHOTO (optional)"); iLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        String[] imgPath = {review[10]};
        HBox imgRow = makeImageRow("📷 Change Image", imgPath, review[10], dlg);

        Label errLbl = makeErrorLabel();

        HBox btnRow = new HBox(12);
        Button cancel = makeBtn("CANCEL", false); cancel.setOnAction(e -> dlg.close());
        Button update = makeBtn("UPDATE REVIEW", true);
        update.setOnAction(e -> {
            errLbl.setVisible(false); errLbl.setManaged(false);
            if (rating[0] < 1) { showErr(errLbl, "Please select a rating."); return; }
            String comment = cArea.getText().trim();
            if (comment.isEmpty()) { showErr(errLbl, "Please write a comment."); return; }
            if (comment.matches(".*[@,.#/\\\\?\"'~`$].*")) { showErr(errLbl, "Comment contains invalid characters."); return; }
            reviewService.updateReviewWithImage(review[0], rating[0], comment, imgPath[0]);
            reviews = new ArrayList<>(reviewService.getReviewsByProductId(productId));
            renderTopRated(); renderReviews(); dlg.close();
        });
        HBox.setHgrow(cancel, Priority.ALWAYS); HBox.setHgrow(update, Priority.ALWAYS);
        btnRow.getChildren().addAll(cancel, update);
        form.getChildren().addAll(rLbl, starRow, cLbl, cArea, noteLbl, iLbl, imgRow, errLbl, btnRow);
        root.getChildren().add(form);
        dlg.setScene(new Scene(root, 520, 560)); dlg.showAndWait();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private HBox makeTitleBar(String text, Stage dlg) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 24, 16, 24));
        bar.setStyle("-fx-border-color:transparent transparent black transparent;-fx-border-width:0 0 1 0;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-letter-spacing:3px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button x = new Button("✕");
        x.setStyle("-fx-background-color:transparent;-fx-font-size:14px;-fx-cursor:hand;");
        x.setOnAction(e -> dlg.close());
        bar.getChildren().addAll(lbl, sp, x);
        return bar;
    }

    private HBox makeImageRow(String btnText, String[] imgPath, String existingPath, Stage dlg) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Button btn = new Button(btnText);
        btn.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-font-size:12px;-fx-cursor:hand;");
        boolean hasExisting = existingPath != null && !existingPath.isEmpty();
        Label nameLabel = new Label(hasExisting ? new File(existingPath).getName() : "No image selected");
        nameLabel.setStyle("-fx-font-size:11px;-fx-text-fill:" + (hasExisting ? "#374151" : "#9ca3af") + ";");
        Button removeBtn = new Button("✕ Remove");
        removeBtn.setStyle("-fx-font-size:11px;-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;");
        removeBtn.setVisible(hasExisting); removeBtn.setManaged(hasExisting);
        removeBtn.setOnAction(e -> { imgPath[0] = null; nameLabel.setText("No image selected"); nameLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;"); removeBtn.setVisible(false); removeBtn.setManaged(false); });
        btn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.gif"));
            File file = fc.showOpenDialog(dlg);
            if (file != null) { imgPath[0] = file.getAbsolutePath(); nameLabel.setText(file.getName()); nameLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;"); removeBtn.setVisible(true); removeBtn.setManaged(true); }
        });
        row.getChildren().addAll(btn, nameLabel, removeBtn);
        return row;
    }

    private void buildStars(HBox row, int[] rating, Label rText, int preselected) {
        for (int i = 1; i <= 5; i++) {
            final int val = i;
            Label star = new Label("★");
            star.setStyle("-fx-font-size:32px;-fx-text-fill:" + (i <= preselected ? "black" : "#d1d5db") + ";-fx-cursor:hand;");
            star.setOnMouseEntered(e -> { for (int j=0;j<row.getChildren().size()-1;j++) ((Label)row.getChildren().get(j)).setStyle("-fx-font-size:32px;-fx-text-fill:"+(j<val?"black":"#d1d5db")+";-fx-cursor:hand;"); });
            star.setOnMouseExited(e ->  { for (int j=0;j<row.getChildren().size()-1;j++) ((Label)row.getChildren().get(j)).setStyle("-fx-font-size:32px;-fx-text-fill:"+(j<rating[0]?"black":"#d1d5db")+";-fx-cursor:hand;"); });
            star.setOnMouseClicked(e -> { rating[0]=val; rText.setText(val+" star"+(val!=1?"s":"")); for (int j=0;j<row.getChildren().size()-1;j++) ((Label)row.getChildren().get(j)).setStyle("-fx-font-size:32px;-fx-text-fill:"+(j<val?"black":"#d1d5db")+";-fx-cursor:hand;"); });
            row.getChildren().add(star);
        }
    }

    private Button makeBtn(String text, boolean primary) {
        Button b = new Button(text); b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(primary ? "-fx-background-color:black;-fx-text-fill:white;-fx-font-size:13px;-fx-padding:10;-fx-cursor:hand;" : "-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-font-size:13px;-fx-padding:10;-fx-cursor:hand;");
        return b;
    }

    private Label makeErrorLabel() {
        Label l = new Label(""); l.setWrapText(true);
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;-fx-background-color:#f3f4f6;-fx-padding:10;-fx-border-color:#d1d5db;-fx-border-width:1;");
        l.setVisible(false); l.setManaged(false); return l;
    }

    private void showErr(Label l, String msg) { l.setText(msg); l.setVisible(true); l.setManaged(true); }

    private void starsOf(HBox box, int rating, int size) {
        box.getChildren().clear();
        for (int i=1;i<=5;i++) {
            Label s = new Label("★");
            s.setStyle(i<=rating?"-fx-text-fill:black;-fx-font-size:"+size+"px;":"-fx-text-fill:#d1d5db;-fx-font-size:"+size+"px;");
            box.getChildren().add(s);
        }
    }

    @FXML private void handleSearch() { renderReviews(); }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            HomePageController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            Stage stage = (Stage) productName.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800)); stage.setTitle("NOCH – Home");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showPerm(String msg) { reviewPermissionMsg.setText(msg); reviewPermissionMsg.setVisible(true); reviewPermissionMsg.setManaged(true); }
    private void hidePerm() { reviewPermissionMsg.setVisible(false); reviewPermissionMsg.setManaged(false); }
}