import React, {useCallback, useEffect, useState} from "react";
import {ScrollView, View} from "react-native";
import {SafeAreaView} from "react-native-safe-area-context";
import {Button, Card, Text, Appbar, ActivityIndicator, useTheme} from "react-native-paper";
import {Badge} from "../../../components/Badge";
import {ProofCard} from "../components/ProofCard";
import * as proofApi from "../api/proofApi";
import {Proof, ProofVerification} from "../types";
import {colors, spacing, typography} from "../../../theme";
import {parseApiError} from "../../../services/apiClient";
export function ProofViewerScreen({route,navigation}:any) {
  const {txId}=route.params;
  const [proof,setProof]=useState<Proof|null>(null), [verification,setVerification]=useState<ProofVerification|null>(null);
  const [loading,setLoading]=useState(true), [verifying,setVerifying]=useState(false), [error,setError]=useState("");
  const theme=useTheme();
  const load=useCallback(async()=>{
    setLoading(true);setError("");setProof(null);setVerification(null);
    try {setProof(await proofApi.getProof(txId));}
    catch(e) {setError(parseApiError(e).message);}
    finally {setLoading(false);}
  },[txId]);
  useEffect(()=>{void load()},[load]);
  async function verify() {
    setVerifying(true);setError("");setVerification(null);
    try {setVerification(await proofApi.verifyProof(txId));}
    catch(e) {setError(parseApiError(e).message);}
    finally {setVerifying(false);}
  }
  const chainReference=proof?.stellarTxHash && /^[a-fA-F0-9]{64}$/.test(proof.stellarTxHash);
  return <SafeAreaView style={{flex:1,backgroundColor:theme.colors.background}}>
    <Appbar.Header><Appbar.BackAction onPress={()=>navigation.goBack()}/><Appbar.Content title="Receipt integrity" subtitle="Evidence and optional chain reference"/></Appbar.Header>
    <ScrollView contentContainerStyle={{padding:spacing.md,paddingBottom:spacing.xl}}>
      <Card style={{backgroundColor:colors.card,borderRadius:16}}><Card.Content>
        <Text style={{...typography.title,color:colors.ink}}>Receipt record</Text>
        <Text style={{color:colors.muted,marginVertical:spacing.sm}}>A receipt hash records payload integrity. It does not independently confirm payment or settlement.</Text>
        {loading&&<ActivityIndicator accessibilityLabel="Loading receipt" style={{margin:spacing.lg}}/>}
        {!!error&&<View accessibilityLiveRegion="polite"><Text style={{color:theme.colors.error,marginVertical:spacing.sm}}>{error}</Text><Button onPress={load} disabled={loading}>Reload receipt</Button></View>}
        {proof&&<>
          <ProofCard proof={proof}/>
          <Text style={{color:colors.muted,marginTop:spacing.md}}>Created: {proof.createdAt || "Not recorded"}</Text>
          <Text style={{color:colors.muted}}>Ledger: {proof.ledger ?? "No ledger recorded"}</Text>
          {!chainReference&&<Text style={{color:colors.muted,marginTop:spacing.md}}>No valid chain reference is available. Receipt integrity has not been independently verified.</Text>}
          <Button mode="contained" onPress={verify} disabled={verifying||!chainReference} loading={verifying} style={{marginTop:spacing.md}}>Check receipt integrity</Button>
          {verification&&<View style={{marginTop:spacing.md}}><Badge label={verification.verified?"Chain integrity checked":"Not verified"} tone={verification.verified?"success":"warning"}/><Text style={{color:colors.muted,marginTop:spacing.sm}}>{verification.reason}</Text></View>}
        </>}
        {!loading&&!proof&&!error&&<Text>No receipt record is available.</Text>}
      </Card.Content></Card>
    </ScrollView>
  </SafeAreaView>;
}
