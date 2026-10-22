import React from "react";
import Svg, {Rect, Path} from "react-native-svg";
export function BrandMark({size=40}:{size?:number}) {
  return <Svg width={size} height={size} viewBox="0 0 64 64" accessibilityLabel="SecurePay shield">
    <Rect width="64" height="64" rx="16" fill="#0B1736"/>
    <Path d="M32 10 49 17v15c0 12-17 23-17 23S15 44 15 32V17Z" fill="none" stroke="#06B6D4" strokeWidth="4"/>
    <Path d="m23 31 7 7 13-15" fill="none" stroke="#FFFFFF" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
  </Svg>;
}
