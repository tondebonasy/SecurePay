import React from "react";
import {Navigate, Route, Routes} from "react-router-dom";
import LoginPage from "./LoginPage";
import DashboardPage from "./DashboardPage";
import {isAuthed} from "../util/auth";
// Evaluate credentials when the protected route mounts after login/navigation.
function RequireAuth({children}:{children:React.ReactNode}) {
  return isAuthed() ? <>{children}</> : <Navigate to="/login" replace/>;
}
export default function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage/>}/>
    <Route path="/" element={<RequireAuth><DashboardPage/></RequireAuth>}/>
    <Route path="*" element={<Navigate to="/" replace/>}/>
  </Routes>;
}
