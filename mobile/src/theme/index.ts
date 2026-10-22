import { MD3LightTheme as DefaultTheme, configureFonts } from 'react-native-paper';
import { colors } from './colors';
import { spacing } from './spacing';
import { typography } from './typography';

const fontConfig = {
  fontFamily: 'Manrope_400Regular',
};

export const theme = {
  ...DefaultTheme,
  roundness: 16,
  colors: {
    ...DefaultTheme.colors,
    background: colors.paper,
    surface: colors.card,
    onBackground: colors.ink,
    onSurface: colors.ink,
    primary: colors.primary,
    accent: colors.secondary,
    outline: colors.border,
    outlineVariant: colors.border,
  },
  fonts: configureFonts({ config: fontConfig, isV3: true }),
  spacing,
  typography,
};

export { colors, spacing, typography };
