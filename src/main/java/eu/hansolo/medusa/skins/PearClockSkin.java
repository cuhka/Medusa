/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;


/**
 * Created by hansolo on 29.01.16.
 */
public class PearClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH      = 250;
    private static final double            PREFERRED_HEIGHT     = 250;
    private static final double            MINIMUM_WIDTH        = 50;
    private static final double            MINIMUM_HEIGHT       = 50;
    private static final double            MAXIMUM_WIDTH        = 1024;
    private static final double            MAXIMUM_HEIGHT       = 1024;
    private static final DateTimeFormatter DATE_TEXT_FORMATER   = DateTimeFormatter.ofPattern("EE");
    private static final DateTimeFormatter DATE_NUMBER_FORMATER = DateTimeFormatter.ofPattern("d");
    private static final DateTimeFormatter TIME_FORMATTER       = DateTimeFormatter.ofPattern("HH:mm");
    private              double            size;
    private              Canvas            ticks;
    private              GraphicsContext   ctx;
    private              Path              hour;
    private              Path              minute;
    private              Path              second;
    private              Text              title;
    private              Text              dateText;
    private              Text              dateNumber;
    private              Text              text;
    private              Pane              pane;
    private              Rotate            hourRotate;
    private              Rotate            minuteRotate;
    private              Rotate            secondRotate;
    private              Group             shadowGroup;
    private              DropShadow        dropShadow;


    // ******************** Constructors **************************************
    public PearClockSkin(Clock clock) {
        super(clock);

        minuteRotate = new Rotate();
        hourRotate   = new Rotate();
        secondRotate = new Rotate();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() < 0 && getSkinnable().getPrefHeight() < 0) {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        ticks = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx   = ticks.getGraphicsContext2D();

        hour  = new Path();
        hour.setFillRule(FillRule.EVEN_ODD);
        hour.setStroke(null);
        hour.getTransforms().setAll(hourRotate);

        minute = new Path();
        minute.setFillRule(FillRule.EVEN_ODD);
        minute.setStroke(null);
        minute.getTransforms().setAll(minuteRotate);

        second = new Path();
        second.setFillRule(FillRule.EVEN_ODD);
        second.setStroke(null);
        second.getTransforms().setAll(secondRotate);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(hour, minute, second);
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(getSkinnable().isTitleVisible());
        title.setManaged(getSkinnable().isTitleVisible());

        dateText = new Text("");
        dateText.setVisible(getSkinnable().isDateVisible());
        dateText.setManaged(getSkinnable().isDateVisible());

        dateNumber = new Text("");
        dateNumber.setVisible(getSkinnable().isDateVisible());
        dateNumber.setManaged(getSkinnable().isDateVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.getChildren().addAll(ticks, title, dateText, dateNumber, text, shadowGroup);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_tYPE) {
        if ("RESIZE".equals(EVENT_tYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_tYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_tYPE)) {
            title.setVisible(getSkinnable().isTitleVisible());
            title.setManaged(getSkinnable().isTitleVisible());
            text.setVisible(getSkinnable().isTextVisible());
            text.setManaged(getSkinnable().isTextVisible());
            dateText.setVisible(getSkinnable().isDateVisible());
            dateText.setManaged(getSkinnable().isDateVisible());
            dateNumber.setVisible(getSkinnable().isDateVisible());
            dateNumber.setManaged(getSkinnable().isDateVisible());
        }
    }


    // ******************** Canvas ********************************************
    private void drawTicks() {
        ctx.clearRect(0, 0, size, size);
        double  sinValue;
        double  cosValue;
        double  startAngle          = 180;
        double  angleStep           = 360 / 240 +0.5;
        Point2D center              = new Point2D(size * 0.5, size * 0.5);
        Color   hourTickMarkColor   = getSkinnable().getHourTickMarkColor();
        Color   minuteTickMarkColor = getSkinnable().getMinuteTickMarkColor();
        Font    font                = Fonts.robotoLight(size * 0.084);
        ctx.setLineCap(StrokeLineCap.BUTT);
        ctx.setFont(font);
        ctx.setLineWidth(size * 0.005);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 239) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.45866667 * sinValue, center.getY() + size * 0.45866667 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.47733333 * sinValue, center.getY() + size * 0.47733333 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.5 * sinValue, center.getY() + size * 0.5 * cosValue);
            Point2D textPoint        = new Point2D(center.getX() + size * 0.405 * sinValue, center.getY() + size * 0.405 * cosValue);
            
            if (counter % 20 == 0) {
                ctx.setStroke(hourTickMarkColor);
                ctx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());

                ctx.save();
                ctx.translate(textPoint.getX(), textPoint.getY());

                Helper.rotateContextForText(ctx, startAngle, angle, TickLabelOrientation.HORIZONTAL);
                ctx.setTextAlign(TextAlignment.CENTER);
                ctx.setTextBaseline(VPos.CENTER);
                ctx.setFill(hourTickMarkColor);
                if (counter == 0) {
                    ctx.fillText("12", 0, 0);
                } else {
                    ctx.fillText(Integer.toString((int) (counter / 20)), 0, 0);
                }

                ctx.restore();
            } else if (counter % 4 == 0) {
                ctx.setStroke(minuteTickMarkColor);
                ctx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (counter % 1 == 0) {
                ctx.setStroke(minuteTickMarkColor);
                ctx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createHourPointer() {
        double width  = size * 0.04533333;
        double height = size * 0.292;
        hour.getElements().clear();
        hour.getElements().add(new MoveTo(0.3235294117647059 * width, 0.9223744292237442 * height));
        hour.getElements().add(new CubicCurveTo(0.3235294117647059 * width, 0.906392694063927 * height,
                                                0.39705882352941174 * width, 0.8949771689497716 * height,
                                                0.5 * width, 0.8949771689497716 * height));
        hour.getElements().add(new CubicCurveTo(0.6029411764705882 * width, 0.8949771689497716 * height,
                                                0.6764705882352942 * width, 0.906392694063927 * height,
                                                0.6764705882352942 * width, 0.9223744292237442 * height));
        hour.getElements().add(new CubicCurveTo(0.6764705882352942 * width, 0.9383561643835616 * height,
                                                0.6029411764705882 * width, 0.9497716894977168 * height,
                                                0.5 * width, 0.9497716894977168 * height));
        hour.getElements().add(new CubicCurveTo(0.39705882352941174 * width, 0.9497716894977168 * height,
                                                0.3235294117647059 * width, 0.9383561643835616 * height,
                                                0.3235294117647059 * width, 0.9223744292237442 * height));
        hour.getElements().add(new ClosePath());
        hour.getElements().add(new MoveTo(0.22058823529411764 * width, 0.0639269406392694 * height));
        hour.getElements().add(new CubicCurveTo(0.22058823529411764 * width, 0.03881278538812785 * height,
                                                0.3382352941176471 * width, 0.02054794520547945 * height,
                                                0.5 * width, 0.02054794520547945 * height));
        hour.getElements().add(new CubicCurveTo(0.6617647058823529 * width, 0.02054794520547945 * height,
                                                0.7794117647058824 * width, 0.03881278538812785 * height,
                                                0.7794117647058824 * width, 0.0639269406392694 * height));
        hour.getElements().add(new CubicCurveTo(0.7794117647058824 * width, 0.0639269406392694 * height,
                                                0.7794117647058824 * width, 0.6484018264840182 * height,
                                                0.7794117647058824 * width, 0.6484018264840182 * height));
        hour.getElements().add(new CubicCurveTo(0.7794117647058824 * width, 0.6735159817351598 * height,
                                                0.6617647058823529 * width, 0.6917808219178082 * height,
                                                0.5 * width, 0.6917808219178082 * height));
        hour.getElements().add(new CubicCurveTo(0.3382352941176471 * width, 0.6917808219178082 * height,
                                                0.22058823529411764 * width, 0.6735159817351598 * height,
                                                0.22058823529411764 * width, 0.6484018264840182 * height));
        hour.getElements().add(new CubicCurveTo(0.22058823529411764 * width, 0.6484018264840182 * height,
                                                0.22058823529411764 * width, 0.0639269406392694 * height,
                                                0.22058823529411764 * width, 0.0639269406392694 * height));
        hour.getElements().add(new ClosePath());
        hour.getElements().add(new MoveTo(0.0, 0.9223744292237442 * height));
        hour.getElements().add(new CubicCurveTo(0.0, 0.9657534246575342 * height,
                                                0.22058823529411764 * width, height,
                                                0.5 * width, height));
        hour.getElements().add(new CubicCurveTo(0.7794117647058824 * width, height,
                                                width, 0.9657534246575342 * height,
                                                width, 0.9223744292237442 * height));
        hour.getElements().add(new CubicCurveTo(width, 0.8881278538812786 * height,
                                                0.8529411764705882 * width, 0.8584474885844748 * height,
                                                0.6617647058823529 * width, 0.8493150684931506 * height));
        hour.getElements().add(new CubicCurveTo(0.6617647058823529 * width, 0.8493150684931506 * height,
                                                0.6617647058823529 * width, 0.7077625570776256 * height,
                                                0.6617647058823529 * width, 0.7077625570776256 * height));
        hour.getElements().add(new CubicCurveTo(0.8088235294117647 * width, 0.6986301369863014 * height,
                                                0.9117647058823529 * width, 0.6757990867579908 * height,
                                                0.9117647058823529 * width, 0.6484018264840182 * height));
        hour.getElements().add(new CubicCurveTo(0.9117647058823529 * width, 0.6484018264840182 * height,
                                                0.9117647058823529 * width, 0.0639269406392694 * height,
                                                0.9117647058823529 * width, 0.0639269406392694 * height));
        hour.getElements().add(new CubicCurveTo(0.9117647058823529 * width, 0.0273972602739726 * height,
                                                0.7352941176470589 * width, 0.0,
                                                0.5 * width, 0.0));
        hour.getElements().add(new CubicCurveTo(0.2647058823529412 * width, 0.0,
                                                0.08823529411764706 * width, 0.0273972602739726 * height,
                                                0.08823529411764706 * width, 0.0639269406392694 * height));
        hour.getElements().add(new CubicCurveTo(0.08823529411764706 * width, 0.0639269406392694 * height,
                                                0.08823529411764706 * width, 0.6484018264840182 * height,
                                                0.08823529411764706 * width, 0.6484018264840182 * height));
        hour.getElements().add(new CubicCurveTo(0.08823529411764706 * width, 0.6757990867579908 * height,
                                                0.19117647058823528 * width, 0.6986301369863014 * height,
                                                0.3382352941176471 * width, 0.7077625570776256 * height));
        hour.getElements().add(new CubicCurveTo(0.3382352941176471 * width, 0.7077625570776256 * height,
                                                0.3382352941176471 * width, 0.8493150684931506 * height,
                                                0.3382352941176471 * width, 0.8493150684931506 * height));
        hour.getElements().add(new CubicCurveTo(0.14705882352941177 * width, 0.8584474885844748 * height,
                                                0.0, 0.8881278538812786 * height,
                                                0.0, 0.9223744292237442 * height));
        hour.getElements().add(new ClosePath());
    }

    private void createMinutePointer() {
        double width  = size * 0.04533333;
        double height = size * 0.488;
        minute.getElements().clear();
        minute.getElements().add(new MoveTo(0.3235294117647059 * width, 0.953551912568306 * height));
        minute.getElements().add(new CubicCurveTo(0.3235294117647059 * width, 0.9439890710382514 * height,
                                                  0.39705882352941174 * width, 0.9371584699453552 * height,
                                                  0.5 * width, 0.9371584699453552 * height));
        minute.getElements().add(new CubicCurveTo(0.6029411764705882 * width, 0.9371584699453552 * height,
                                                  0.6764705882352942 * width, 0.9439890710382514 * height,
                                                  0.6764705882352942 * width, 0.953551912568306 * height));
        minute.getElements().add(new CubicCurveTo(0.6764705882352942 * width, 0.9631147540983607 * height,
                                                  0.6029411764705882 * width, 0.9699453551912568 * height,
                                                  0.5 * width, 0.9699453551912568 * height));
        minute.getElements().add(new CubicCurveTo(0.39705882352941174 * width, 0.9699453551912568 * height,
                                                  0.3235294117647059 * width, 0.9631147540983607 * height,
                                                  0.3235294117647059 * width, 0.953551912568306 * height));
        minute.getElements().add(new ClosePath());
        minute.getElements().add(new MoveTo(0.22058823529411764 * width, 0.03825136612021858 * height));
        minute.getElements().add(new CubicCurveTo(0.22058823529411764 * width, 0.02459016393442623 * height,
                                                  0.35294117647058826 * width, 0.012295081967213115 * height,
                                                  0.5 * width, 0.012295081967213115 * height));
        minute.getElements().add(new CubicCurveTo(0.6470588235294118 * width, 0.012295081967213115 * height,
                                                  0.7794117647058824 * width, 0.02459016393442623 * height,
                                                  0.7794117647058824 * width, 0.03825136612021858 * height));
        minute.getElements().add(new CubicCurveTo(0.7794117647058824 * width, 0.03825136612021858 * height,
                                                  0.7794117647058824 * width, 0.7896174863387978 * height,
                                                  0.7794117647058824 * width, 0.7896174863387978 * height));
        minute.getElements().add(new CubicCurveTo(0.7794117647058824 * width, 0.8032786885245902 * height,
                                                  0.6470588235294118 * width, 0.8155737704918032 * height,
                                                  0.5 * width, 0.8155737704918032 * height));
        minute.getElements().add(new CubicCurveTo(0.35294117647058826 * width, 0.8155737704918032 * height,
                                                  0.22058823529411764 * width, 0.8032786885245902 * height,
                                                  0.22058823529411764 * width, 0.7896174863387978 * height));
        minute.getElements().add(new CubicCurveTo(0.22058823529411764 * width, 0.7896174863387978 * height,
                                                  0.22058823529411764 * width, 0.03825136612021858 * height,
                                                  0.22058823529411764 * width, 0.03825136612021858 * height));
        minute.getElements().add(new ClosePath());
        minute.getElements().add(new MoveTo(0.0, 0.953551912568306 * height));
        minute.getElements().add(new CubicCurveTo(0.0, 0.9795081967213115 * height,
                                                  0.22058823529411764 * width, height,
                                                  0.5 * width, height));
        minute.getElements().add(new CubicCurveTo(0.7794117647058824 * width, height,
                                                  width, 0.9795081967213115 * height,
                                                  width, 0.953551912568306 * height));
        minute.getElements().add(new CubicCurveTo(width, 0.9330601092896175 * height,
                                                  0.8529411764705882 * width, 0.9153005464480874 * height,
                                                  0.6617647058823529 * width, 0.9098360655737705 * height));
        minute.getElements().add(new CubicCurveTo(0.6617647058823529 * width, 0.9098360655737705 * height,
                                                  0.6617647058823529 * width, 0.825136612021858 * height,
                                                  0.6617647058823529 * width, 0.825136612021858 * height));
        minute.getElements().add(new CubicCurveTo(0.8088235294117647 * width, 0.8183060109289617 * height,
                                                  0.9117647058823529 * width, 0.8060109289617486 * height,
                                                  0.9117647058823529 * width, 0.7896174863387978 * height));
        minute.getElements().add(new CubicCurveTo(0.9117647058823529 * width, 0.7896174863387978 * height,
                                                  0.9117647058823529 * width, 0.03825136612021858 * height,
                                                  0.9117647058823529 * width, 0.03825136612021858 * height));
        minute.getElements().add(new CubicCurveTo(0.9117647058823529 * width, 0.017759562841530054 * height,
                                                  0.7205882352941176 * width, 0.0,
                                                  0.5 * width, 0.0));
        minute.getElements().add(new CubicCurveTo(0.27941176470588236 * width, 0.0,
                                                  0.08823529411764706 * width, 0.017759562841530054 * height,
                                                  0.08823529411764706 * width, 0.03825136612021858 * height));
        minute.getElements().add(new CubicCurveTo(0.08823529411764706 * width, 0.03825136612021858 * height,
                                                  0.08823529411764706 * width, 0.7896174863387978 * height,
                                                  0.08823529411764706 * width, 0.7896174863387978 * height));
        minute.getElements().add(new CubicCurveTo(0.08823529411764706 * width, 0.8060109289617486 * height,
                                                  0.19117647058823528 * width, 0.8183060109289617 * height,
                                                  0.3382352941176471 * width, 0.825136612021858 * height));
        minute.getElements().add(new CubicCurveTo(0.3382352941176471 * width, 0.825136612021858 * height,
                                                  0.3382352941176471 * width, 0.9098360655737705 * height,
                                                  0.3382352941176471 * width, 0.9098360655737705 * height));
        minute.getElements().add(new CubicCurveTo(0.14705882352941177 * width, 0.9153005464480874 * height,
                                                  0.0, 0.9330601092896175 * height,
                                                  0.0, 0.953551912568306 * height));
        minute.getElements().add(new ClosePath());
    }

    private void createSecondPointer() {
        double width  = size * 0.02933333;
        double height = size * 0.58133333;
        second.getElements().clear();
        second.getElements().add(new MoveTo(0.22727272727272727 * width, 0.8600917431192661 * height));
        second.getElements().add(new CubicCurveTo(0.22727272727272727 * width, 0.8520642201834863 * height,
                                                  0.3409090909090909 * width, 0.8463302752293578 * height,
                                                  0.5 * width, 0.8463302752293578 * height));
        second.getElements().add(new CubicCurveTo(0.6590909090909091 * width, 0.8463302752293578 * height,
                                                  0.7727272727272727 * width, 0.8520642201834863 * height,
                                                  0.7727272727272727 * width, 0.8600917431192661 * height));
        second.getElements().add(new CubicCurveTo(0.7727272727272727 * width, 0.8681192660550459 * height,
                                                  0.6590909090909091 * width, 0.8738532110091743 * height,
                                                  0.5 * width, 0.8738532110091743 * height));
        second.getElements().add(new CubicCurveTo(0.3409090909090909 * width, 0.8738532110091743 * height,
                                                  0.22727272727272727 * width, 0.8681192660550459 * height,
                                                  0.22727272727272727 * width, 0.8600917431192661 * height));
        second.getElements().add(new ClosePath());
        second.getElements().add(new MoveTo(0.0, 0.8600917431192661 * height));
        second.getElements().add(new CubicCurveTo(0.0, 0.8715596330275229 * height,
                                                  0.1590909090909091 * width, 0.8818807339449541 * height,
                                                  0.38636363636363635 * width, 0.8841743119266054 * height));
        second.getElements().add(new CubicCurveTo(0.38636363636363635 * width, 0.8841743119266054 * height,
                                                  0.38636363636363635 * width, height,
                                                  0.38636363636363635 * width, height));
        second.getElements().add(new LineTo(0.6136363636363636 * width, height));
        second.getElements().add(new CubicCurveTo(0.6136363636363636 * width, height,
                                                  0.6136363636363636 * width, 0.8841743119266054 * height,
                                                  0.6136363636363636 * width, 0.8841743119266054 * height));
        second.getElements().add(new CubicCurveTo(0.8409090909090909 * width, 0.8818807339449541 * height,
                                                  width, 0.8715596330275229 * height,
                                                  width, 0.8600917431192661 * height));
        second.getElements().add(new CubicCurveTo(width, 0.8486238532110092 * height,
                                                  0.8409090909090909 * width, 0.838302752293578 * height,
                                                  0.6136363636363636 * width, 0.8360091743119266 * height));
        second.getElements().add(new CubicCurveTo(0.6136363636363636 * width, 0.8360091743119266 * height,
                                                  0.6136363636363636 * width, 0.0,
                                                  0.6136363636363636 * width, 0.0));
        second.getElements().add(new LineTo(0.38636363636363635 * width, 0.0));
        second.getElements().add(new CubicCurveTo(0.38636363636363635 * width, 0.0,
                                                  0.38636363636363635 * width, 0.8360091743119266 * height,
                                                  0.38636363636363635 * width, 0.8360091743119266 * height));
        second.getElements().add(new CubicCurveTo(0.1590909090909091 * width, 0.838302752293578 * height,
                                                  0.0, 0.8486238532110092 * height,
                                                  0.0, 0.8600917431192661 * height));
        second.getElements().add(new ClosePath());
    }


    // ******************** Resizing ******************************************
    public void updateTime(final LocalDateTime TIME) {
        if (getSkinnable().isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (getSkinnable().isDiscreteSeconds()) {
            secondRotate.setAngle(TIME.getSecond() * 6);
        } else {
            secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
        }

        hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));

        text.setText(TIME_FORMATTER.format(TIME));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateText.setText(DATE_TEXT_FORMATER.format(TIME).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.4), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

        dateNumber.setText(DATE_NUMBER_FORMATER.format(TIME).toUpperCase());
        Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
        dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.51), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            ticks.setWidth(size);
            ticks.setHeight(size);
            drawTicks();

            createHourPointer();
            hour.setFill(getSkinnable().getHourNeedleColor());
            hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, size * 0.23066667);

            createMinutePointer();
            minute.setFill(getSkinnable().getMinuteNeedleColor());
            minute.relocate((size - minute.getLayoutBounds().getWidth()) * 0.5, size * 0.03466667);

            createSecondPointer();
            second.setFill(getSkinnable().getSecondNeedleColor());
            second.relocate((size - second.getLayoutBounds().getWidth()) * 0.5, 0);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateText.setFill(getSkinnable().getDateColor());
            dateText.setFont(Fonts.latoLight(size * 0.06666667));
            dateText.relocate((center - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.4), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

            dateNumber.setFill(getSkinnable().getSecondNeedleColor());
            dateNumber.setFont(Fonts.latoLight(size * 0.06666667));
            dateNumber.relocate((center - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.51), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(getSkinnable().getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            hourRotate.setPivotX(hour.getLayoutBounds().getWidth() * 0.5);
            hourRotate.setPivotY(hour.getLayoutBounds().getHeight() * 0.92237443);
            minuteRotate.setPivotX(minute.getLayoutBounds().getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getLayoutBounds().getHeight() * 0.95355191);
            secondRotate.setPivotX(second.getLayoutBounds().getWidth() * 0.5);
            secondRotate.setPivotY(second.getLayoutBounds().getHeight() * 0.86009174);
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        LocalDateTime time = getSkinnable().getTime();

        updateTime(time);

        title.setText(getSkinnable().getTitle());
        Helper.adjustTextSize(title, 0.6 * size, size * 0.12);
        title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

        text.setText(TIME_FORMATTER.format(time));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateText.setText(DATE_TEXT_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.4), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

        dateNumber.setText(DATE_NUMBER_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
        dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.51), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);
    }
}
