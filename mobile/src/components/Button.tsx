import React from "react";
import {ActivityIndicator,Pressable,Text,View,type TextStyle,type ViewStyle} from "react-native";
import {cva} from "class-variance-authority";
import {typography} from "../theme";
const variants=cva('min-h-12 flex-row items-center justify-center rounded-xl px-5 py-3',{variants:{variant:{primary:'bg-primary active:opacity-80',secondary:'border border-primary bg-transparent active:bg-blue-50',ghost:'bg-transparent active:bg-blue-50'},disabled:{true:'opacity-50',false:''}},defaultVariants:{variant:'primary',disabled:false}});
type Props={label:string;onPress:()=>void;variant?:'primary'|'secondary'|'ghost';disabled?:boolean;style?:ViewStyle;contentStyle?:ViewStyle;labelStyle?:TextStyle;loading?:boolean};
// Shared native button follows the React Native Reusables composition pattern.
export function Button({label,onPress,variant='primary',disabled=false,style,contentStyle,labelStyle,loading=false}:Props){const inactive=disabled||loading;return <Pressable accessibilityRole="button" accessibilityLabel={label} accessibilityState={{disabled:inactive,busy:loading}} onPress={onPress} disabled={inactive} className={variants({variant,disabled:inactive})} style={style}><View className="flex-row items-center justify-center gap-2" style={contentStyle}>{loading&&<ActivityIndicator size="small" color={variant==='primary'?'#fff':'#2456E6'}/>}<Text className={variant==='primary'?'text-white':'text-primary'} style={[typography.subtitle,labelStyle]}>{label}</Text></View></Pressable>}
