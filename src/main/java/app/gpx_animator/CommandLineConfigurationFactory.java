/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class CommandLineConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineConfigurationFactory.class);
    private final List<TrackIcon> trackIconList = new ArrayList<>();
    private final List<Boolean> mirrorTrackIconList = new ArrayList<>();
    private final List<String> inputGpxList = new ArrayList<>();

    private final List<String> inputIconList = new ArrayList<>();

    private final List<Color> colorList = new ArrayList<>();

    private final List<Float> lineWidthList = new ArrayList<>();

    private final boolean gui;


    private final Configuration configuration;


    @SuppressWarnings("checkstyle:MethodLength") // Is it worth investing time refactoring this class?
    public CommandLineConfigurationFactory(final String[] args) throws UserException {
        final ResourceBundle resourceBundle = Preferences.getResourceBundle();

        final Configuration.Builder cfg = Configuration.createBuilder();

        boolean forceGui = false;

        final List<String> labelList = new ArrayList<>();
        final List<Long> timeOffsetList = new ArrayList<>();
        final List<Long> forcedPointIntervalList = new ArrayList<>();


        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            LOGGER.info("Checking option '{}'", arg);

            try {
                final Option option = arg.startsWith("--") ? Option.fromName(arg.substring(2)) : null;

                if (option == null) {
                    throw new UserException(String.format(resourceBundle.getString("cli.error.option"), arg));
                } else {
                    switch (option) {
                        case ATTRIBUTION -> cfg.attribution(args[++i]);
                        case BACKGROUND_MAP_VISIBILITY -> cfg.backgroundMapVisibility(Float.parseFloat(args[++i]));
                        case COLOR -> colorList.add(Color.decode(args[++i]));
                        case BACKGROUND_COLOR -> {
                            final long lv = Long.decode(args[++i]);
                            cfg.backgroundColor(new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true));
                        }
                        case FLASHBACK_COLOR -> {
                            final long lv1 = Long.decode(args[++i]);
                            cfg.flashbackColor(new Color(lv1 < Integer.MAX_VALUE ? (int) lv1 : (int) (0xffffffff00000000L | lv1), true));
                        }
                        case FLASHBACK_DURATION -> {
                            final String s = args[++i];
                            cfg.flashbackDuration(s.trim().isEmpty() ? null : Long.parseLong(s)); // NOPMD -- null = not set
                        }
                        case FONT -> cfg.font(new FontXmlAdapter().unmarshal(args[++i]));
                        case FORCED_POINT_TIME_INTERVAL -> {
                            final String s1 = args[++i].trim();
                            forcedPointIntervalList.add(s1.isEmpty() ? null : Long.valueOf(s1)); // NOPMD -- null = not set
                        }
                        case FPS -> cfg.fps(Double.parseDouble(args[++i]));
                        case GUI -> {
                            if (GraphicsEnvironment.isHeadless()) {
                                throw new UserException(resourceBundle.getString("cli.error.graphics"));
                            }
                            forceGui = true;
                        }
                        case HEIGHT -> cfg.height(Integer.valueOf(args[++i]));
                        case HELP -> {
                            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
                                pw.println(Constants.APPNAME_VERSION);
                                pw.println(Constants.COPYRIGHT);
                                pw.println();
                                pw.println(resourceBundle.getString("cli.help.usage"));
                                Help.printHelp(new Help.PrintWriterOptionHelpWriter(pw));
                                pw.flush();
                            }
                            exit();
                        }
                        case INPUT -> inputGpxList.add(args[++i]);
                        case TRACK_ICON -> trackIconList.add(new TrackIcon(args[++i]));
                        case TRACK_ICON_FILE -> inputIconList.add(args[++i]);
                        case TRACK_ICON_MIRROR -> mirrorTrackIconList.add(true);
                        case KEEP_IDLE -> cfg.skipIdle(false);
                        case LABEL -> labelList.add(args[++i]);
                        case LINE_WIDTH -> lineWidthList.add(Float.valueOf(args[++i]));
                        case MARGIN -> cfg.margin(Integer.parseInt(args[++i]));
                        case MARKER_SIZE -> cfg.markerSize(Double.parseDouble(args[++i]));
                        case MAX_LAT -> cfg.maxLat(Double.parseDouble(args[++i]));
                        case MAX_LON -> cfg.maxLon(Double.parseDouble(args[++i]));
                        case MIN_LAT -> cfg.minLat(Double.parseDouble(args[++i]));
                        case MIN_LON -> cfg.minLon(Double.parseDouble(args[++i]));
                        case OUTPUT -> cfg.output(new File(args[++i]));
                        case LOGO -> cfg.logo(new File(args[++i]));
                        case PHOTO_DIR -> cfg.photoDirectory(args[++i]);
                        case PHOTO_TIME -> cfg.photoTime(Long.parseLong(args[++i]));
                        case PHOTO_ANIMATION_DURATION -> cfg.photoAnimationDuration(Long.parseLong(args[++i]));
                        case PRE_DRAW_TRACK -> cfg.preDrawTrack(true);
                        case PRE_DRAW_TRACK_COLOR -> {
                            cfg.preDrawTrackColor(Color.decode(args[++i]));
                        }
                        case SPEEDUP -> cfg.speedup(Double.parseDouble(args[++i]));
                        case SPEED_UNIT -> {
                            // not yet implemented
                            cfg.speedUnit(SpeedUnit.KMH);
                            ++i;
                        }
                        case TAIL_DURATION -> cfg.tailDuration(Long.parseLong(args[++i]));
                        case TAIL_COLOR -> {
                            final long lvTailColor = Long.decode(args[++i]);
                            cfg.tailColor(new Color(lvTailColor < Integer.MAX_VALUE
                                    ? (int) lvTailColor : (int) (0xffffffff00000000L | lvTailColor), true));
                        }
                        case TIME_OFFSET -> {
                            final String s2 = args[++i].trim();
                            timeOffsetList.add(s2.isEmpty() ? null : Long.valueOf(s2)); // NOPMD -- null = not set
                        }
                        case TMS_URL_TEMPLATE -> cfg.tmsUrlTemplate(args[++i]);
                        case TOTAL_TIME -> {
                            final String s3 = args[++i].trim();
                            cfg.totalTime(s3.isEmpty() ? null : Long.valueOf(s3)); // NOPMD -- null = not set
                        }
                        case VIEWPORT_WIDTH -> cfg.viewportWidth(Integer.valueOf(args[++i]));
                        case VIEWPORT_HEIGHT -> cfg.viewportHeight(Integer.valueOf(args[++i]));
                        case WAYPOINT_SIZE -> cfg.waypointSize(Double.parseDouble(args[++i]));
                        case WIDTH -> cfg.width(Integer.valueOf(args[++i]));
                        case ZOOM -> cfg.zoom(Integer.parseInt(args[++i]));
                        default -> {
                            LOGGER.error("Unknown option: {}", arg);
                            throw new AssertionError();
                        }
                    }

                    // TODO --configuration : args[++i];
                }
            } catch (final NumberFormatException e) {
                throw new UserException(String.format(resourceBundle.getString("cli.error.number"), arg));
            } catch (final ArrayIndexOutOfBoundsException e) {
                throw new UserException(String.format(resourceBundle.getString("cli.error.parameter"), arg));
            }
        }

        normalizeColors();
        normalizeLineWidths();
        normalizeTrackIcons();
        normalizeInputIcons();
        normalizeMirrorTrackIcons();

        for (int i = 0, n = inputGpxList.size(); i < n; i++) {
            final TrackConfiguration.Builder tcb = TrackConfiguration.createBuilder();
            tcb.inputGpx(new File(inputGpxList.get(i)));
            tcb.color(colorList.get(i));
            tcb.lineWidth(lineWidthList.get(i));
            tcb.label(i < labelList.size() ? labelList.get(i) : "");
            tcb.timeOffset(i < timeOffsetList.size() ? timeOffsetList.get(i) : Long.valueOf(0));
            tcb.forcedPointInterval(i < forcedPointIntervalList.size() ? forcedPointIntervalList.get(i) : null); // NOPMD -- null = not set
            if (!trackIconList.isEmpty()) {
                tcb.trackIcon(trackIconList.get(i));
            }
            tcb.inputIcon(new File(inputIconList.get(i)));
            tcb.mirrorTrackIcon(mirrorTrackIconList.get(i));

            cfg.addTrackConfiguration(tcb.build());
        }

        gui = args.length == 0 || forceGui;

        configuration = cfg.build();

        LOGGER.info("configuration={}", configuration.toString());
    }

    @SuppressWarnings({"PMD.DoNotCallSystemExit", "DuplicateStringLiteralInspection"}) // Exit after printing command line help message
    @SuppressFBWarnings(value = "DM_EXIT", justification = "Exit after printing command line help message") //NON-NLS
    private void exit() {
        System.exit(0);
    }

    private void normalizeColors() {
        final int size = inputGpxList.size();
        final int size2 = colorList.size();
        if (size2 == 0) {
            for (int i = 0; i < size; i++) {
                colorList.add(Color.getHSBColor((float) i / size, 0.8f, 1f));
            }
        } else if (size2 < size) {
            for (int i = size2; i < size; i++) {
                colorList.add(colorList.get(i - size2));
            }
        }
    }


    private void normalizeLineWidths() {
        final int size = inputGpxList.size();
        final int size2 = lineWidthList.size();
        if (size2 == 0) {
            for (int i = 0; i < size; i++) {
                lineWidthList.add(2f);
            }
        } else if (size2 < size) {
            for (int i = size2; i < size; i++) {
                lineWidthList.add(lineWidthList.get(i - size2));
            }
        }
    }

    private void normalizeTrackIcons() {
        final int size = inputGpxList.size();
        final int size2 = trackIconList.size();
        if (size2 == 0) {
            for (int i = 0; i < size; i++) {
                trackIconList.add(new TrackIcon("", ""));
            }
        } else if (size2 < size) {
            for (int i = size2; i < size; i++) {
                trackIconList.add(trackIconList.get(i - size2));
            }
        }
    }

    private void normalizeInputIcons() {
        final int size = inputGpxList.size();
        final int size2 = inputIconList.size();
        if (size2 == 0) {
            for (int i = 0; i < size; i++) {
                inputIconList.add("dummy-input-icon");
            }
        } else if (size2 < size) {
            for (int i = size2; i < size; i++) {
                inputIconList.add(inputIconList.get(i - size2));
            }
        }
    }

    private void normalizeMirrorTrackIcons() {
        final int size = inputGpxList.size();
        final int size2 = mirrorTrackIconList.size();
        if (size2 == 0) {
            for (int i = 0; i < size; i++) {
                mirrorTrackIconList.add(false);
            }
        } else if (size2 < size) {
            for (int i = size2; i < size; i++) {
                mirrorTrackIconList.add(mirrorTrackIconList.get(i - size2));
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }


    public boolean isGui() {
        return gui;
    }

}
