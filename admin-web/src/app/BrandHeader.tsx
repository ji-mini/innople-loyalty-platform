import { Link } from 'react-router-dom'
import innopleLogo from '../assets/innople-logo.png'

export function BrandHeader(props: { variant?: 'fixed' | 'sider' }) {
  const variant = props.variant ?? 'fixed'
  return (
    <div className={`innople-brand-header innople-brand-header--${variant}`}>
      <Link to="/dashboard" className="innople-brand-link" aria-label="INNOPLE">
        <img className="innople-brand-logo" src={innopleLogo} alt="INNOPLE" />
        <span className="innople-brand-title">INNO MEMBERSHIP ADMIN</span>
      </Link>
    </div>
  )
}

